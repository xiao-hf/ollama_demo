package com.xiao.controller;

import com.alibaba.fastjson.JSON;
import com.xiao.dto.ChatRequestDTO;
import com.xiao.dto.OllamaRequestDTO;
import com.xiao.dto.OllamaResponseDTO;
import com.xiao.utils.HttpUtil;
import com.xiao.utils.OllamaStreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.annotation.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ollama大模型API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ollama")
public class OllamaController {

    @Resource
    private HttpUtil httpUtil;
    
    @Resource
    private OllamaStreamUtil ollamaStreamUtil;
    
    // 存储所有活跃的SSE连接
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    /**
     * 普通请求大模型API (POST方式)
     * 
     * @param request 聊天请求DTO，包含text和model参数
     * @return 大模型的回复
     */
    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody ChatRequestDTO request) {

        log.info("收到普通请求，模型: {}, 内容: {}", request.getModel(), request.getText());
        
        // Ollama API地址
        String url = "http://localhost:11434/api/generate";
        
        // 构建请求DTO
        OllamaRequestDTO ollamaRequest = OllamaRequestDTO.builder()
                .model(request.getModel())
                .prompt(request.getText())
                .stream(false)
                .build();
        
        // 设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        
        // 发送POST请求
        String responseJson = httpUtil.doPost(url, headers, JSON.toJSONString(ollamaRequest));
        
        // 解析响应
        OllamaResponseDTO response = JSON.parseObject(responseJson, OllamaResponseDTO.class);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("model", response.getModel());
        result.put("response", response.getResponse());
        result.put("done", response.getDone());
        result.put("total_duration", response.getTotal_duration());
        
        return result;
    }
    
    /**
     * 流式请求大模型API (POST方式)
     * 使用SSE实现实时流式响应
     * 
     * @param request 聊天请求DTO，包含text和model参数
     * @return SSE事件流
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAsk(@RequestBody ChatRequestDTO request) {
        
        log.info("收到流式请求，模型: {}, 内容: {}", request.getModel(), request.getText());
        
        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);
        String emitterId = String.valueOf(System.currentTimeMillis());
        emitters.put(emitterId, emitter);
        
        // 设置完成、超时和错误回调，以便清理资源
        emitter.onCompletion(() -> emitters.remove(emitterId));
        emitter.onTimeout(() -> emitters.remove(emitterId));
        emitter.onError(e -> emitters.remove(emitterId));
        
        // 使用字符级别流式响应
        ollamaStreamUtil.streamRequestCharByChar(
                request.getModel(),
                request.getText(),
                // 处理每个字符
                character -> {
                    try {
                        // 发送单个字符作为SSE事件
                        emitter.send(SseEmitter.event()
                                .name("character")
                                .data(character.toString()));
                    } catch (IOException e) {
                        log.error("发送SSE事件失败: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                // 完成回调
                () -> {
                    try {
                        // 发送完成事件
                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data("true"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送完成事件失败: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                // 错误回调
                error -> {
                    log.error("流式请求发生错误: {}", error.getMessage());
                    emitter.completeWithError(error);
                },
                0  // 不设置字符间延迟，让前端控制显示速度
        );
        
        return emitter;
    }
    
    /**
     * 为了兼容现有前端，保留GET方式的流式请求API
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAskGet(
            @RequestParam String text,
            @RequestParam(required = false, defaultValue = "deepseek-r1:8b") String model) {
        
        ChatRequestDTO request = new ChatRequestDTO(text, model);
        return streamAsk(request);
    }
} 