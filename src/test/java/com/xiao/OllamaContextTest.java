package com.xiao;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiao.dto.OllamaRequestDTO;
import com.xiao.dto.OllamaResponseDTO;
import com.xiao.utils.HttpUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ollama上下文对话测试
 * 用于测试上下文对话功能是否正常工作
 */
@SpringBootTest
public class OllamaContextTest {

    @Resource
    HttpUtil httpUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Ollama API地址
    private static final String url = "http://localhost:11434/api/generate";
    
    /**
     * 测试上下文对话功能
     * 发送两条消息：
     * 1. 第一条消息是数字"1"
     * 2. 第二条消息询问"我上一句话对你说了什么"
     * 如果上下文功能正常，第二条消息的回复应包含对数字"1"的引用
     */
    @Test
    public void testContextContinuity() throws Exception {
        // 测试参数
        String modelName = "deepseek-r1:8b";
        String firstMessage = "1";
        String secondMessage = "我上一句话对你说了什么";

        List<Integer> context = new ArrayList<>();
        String str = sendMessage(firstMessage, modelName, context);
        System.out.println(str);

        OllamaResponseDTO resp = JSON.parseObject(str, OllamaResponseDTO.class);
        context = resp.getContext();

        str = sendMessage(secondMessage, modelName, context);
        System.out.println(str);
        resp = JSON.parseObject(str, OllamaResponseDTO.class);

    }
    
    /**
     * 发送消息并获取完整响应
     * 
     * @param text 消息内容
     * @param model 模型名称
     * @param context 上下文(首次对话为null)
     * @return 响应文本
     */
    private String sendMessage(String text, String model, List<Integer> context) {
        OllamaRequestDTO request = OllamaRequestDTO.builder()
                .model("deepseek-r1:8b")  // 使用deepseek-r1:8b模型
                .prompt("你好")           // 设置提示为"你好"
                .stream(false)// 不使用流式响应
                .context(context)
                .build();
        // 设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return httpUtil.doPost(url, headers, JSON.toJSONString(request));
    }
} 