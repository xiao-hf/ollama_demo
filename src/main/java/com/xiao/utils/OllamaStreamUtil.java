package com.xiao.utils;

import com.alibaba.fastjson.JSON;
import com.xiao.dto.OllamaRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ollama流式响应处理工具类
 */
@Slf4j
@Component
public class OllamaStreamUtil {

    private final WebClient webClient;
    
    public OllamaStreamUtil() {
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * 发送流式请求到Ollama API（行级别输出）
     *
     * @param model 模型名称
     * @param prompt 提问内容
     * @param context 上下文信息
     * @param onMessage 每个响应片段的处理函数
     * @param onComplete 完成时的回调函数，带上下文参数
     * @param onError 错误处理函数
     */
    public void streamRequest(String model, String prompt, List<Integer> context,
                             Consumer<String> onMessage, 
                             Consumer<List<Integer>> onComplete, 
                             Consumer<Throwable> onError) {
        // 构建请求对象
        OllamaRequestDTO request = OllamaRequestDTO.builder()
                .model(model)
                .prompt(prompt)
                .context(context)  // 添加上下文
                .stream(true)  // 启用流式响应
                .build();
        
        // 发送请求并处理流式响应
        webClient.post()
                .uri("http://localhost:11434/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSON.toJSONString(request))
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(chunk -> {
                    try {
                        // 每个chunk是一个JSON字符串
                        StreamResponse response = JSON.parseObject(chunk, StreamResponse.class);
                        if (response != null) {
                            if (response.getResponse() != null) {
                                return Flux.just(response.getResponse());
                            }
                            // 如果是最后一个响应，保存上下文
                            if (response.getDone() != null && response.getDone() && response.getContext() != null) {
                                onComplete.accept(response.getContext());
                                return Flux.empty();
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析响应失败: {}", e.getMessage());
                    }
                    return Flux.empty();
                })
                .subscribe(
                        onMessage,       // 每收到一个片段都会调用
                        onError,         // 发生错误时调用
                        () -> {
                            // 如果没有上下文，也要调用完成回调
                            onComplete.accept(null);
                        }
                );
    }
    
    /**
     * 发送流式请求到Ollama API（字符级别输出，实现打字机效果）
     *
     * @param model 模型名称
     * @param prompt 提问内容
     * @param context 上下文信息
     * @param onCharacter 每个字符的处理函数
     * @param onComplete 完成时的回调函数，带上下文参数
     * @param onError 错误处理函数
     * @param charDelayMs 字符间延迟（毫秒），设为0则不延迟
     */
    public void streamRequestCharByChar(String model, String prompt, List<Integer> context,
                             Consumer<Character> onCharacter, 
                             Consumer<List<Integer>> onComplete, 
                             Consumer<Throwable> onError,
                             int charDelayMs) {
        // 构建请求对象
        OllamaRequestDTO request = OllamaRequestDTO.builder()
                .model(model)
                .prompt(prompt)
                .context(context)  // 添加上下文
                .stream(true)  // 启用流式响应
                .build();
        
        // 保存最后一次响应的上下文
        final List<Integer>[] lastContext = new List[1];
        
        // 发送请求并处理流式响应
        webClient.post()
                .uri("http://localhost:11434/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSON.toJSONString(request))
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(chunk -> {
                    try {
                        // 每个chunk是一个JSON字符串
                        StreamResponse response = JSON.parseObject(chunk, StreamResponse.class);
                        if (response != null) {
                            // 保存上下文
                            if (response.getContext() != null) {
                                lastContext[0] = response.getContext();
                            }
                            
                            if (response.getResponse() != null) {
                                String text = response.getResponse();
                                
                                // 将文本拆分为字符数组
                                Character[] chars = new Character[text.length()];
                                for (int i = 0; i < text.length(); i++) {
                                    chars[i] = text.charAt(i);
                                }
                                
                                if (charDelayMs > 0) {
                                    // 有延迟的字符流
                                    return Flux.fromArray(chars)
                                            .delayElements(Duration.ofMillis(charDelayMs));
                                } else {
                                    // 无延迟的字符流
                                    return Flux.fromArray(chars);
                                }
                            }
                            
                            // 如果是最后一个响应，检查是否完成
                            if (response.getDone() != null && response.getDone()) {
                                return Flux.empty();
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析响应失败: {}", e.getMessage());
                    }
                    return Flux.empty();
                })
                .subscribe(
                        onCharacter,     // 每个字符都会调用
                        onError,         // 发生错误时调用
                        () -> {
                            // 完成时调用，传递上下文
                            onComplete.accept(lastContext[0]);
                        }
                );
    }
    
    /**
     * 简化版的流式响应对象
     */
    @lombok.Data
    private static class StreamResponse {
        private String model;
        private String response;
        private Boolean done;
        private List<Integer> context;
    }
} 