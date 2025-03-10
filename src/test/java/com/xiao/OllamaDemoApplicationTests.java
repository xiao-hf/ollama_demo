package com.xiao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.xiao.dto.OllamaRequestDTO;
import com.xiao.dto.OllamaResponseDTO;
import com.xiao.mapper.UserMapper;
import com.xiao.utils.HttpUtil;
import com.xiao.utils.OllamaStreamUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class OllamaDemoApplicationTests {

	@Resource
	UserMapper userMapper;

	@Resource
	HttpUtil httpUtil;
	
	@Resource
	OllamaStreamUtil ollamaStreamUtil;

	@Test
	void testAsk() {
		// Ollama API地址
		String url = "http://localhost:11434/api/generate";
		
		// 构建请求DTO
		OllamaRequestDTO request = OllamaRequestDTO.builder()
				.model("deepseek-r1:8b")  // 使用deepseek-r1:8b模型
				.prompt("你好")           // 设置提示为"你好"
				.stream(false)// 不使用流式响应
				.build();
		
		// 设置请求头
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		
		// 将请求对象转换为JSON字符串
		String requestJson = JSON.toJSONString(request);
		
		// 发送POST请求
		String responseJson = httpUtil.doPost(url, headers, requestJson);
		
		// 配置FastJSON，将时间戳自动转换为Date
		JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";
		
		// 将响应JSON解析为DTO对象
		OllamaResponseDTO response = JSON.parseObject(responseJson, OllamaResponseDTO.class, Feature.AllowISO8601DateFormat);
		
		// 打印响应结果
		System.out.println("模型: " + response.getModel());
		System.out.println("创建时间: " + (response.getCreated_at() != null ? 
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(response.getCreated_at()) : "无"));
		System.out.println("回复: " + response.getResponse());
		System.out.println("是否完成: " + response.getDone());
		System.out.println("完成原因: " + response.getDone_reason());
		System.out.println("总耗时(ns): " + response.getTotal_duration());
		System.out.println("上下文tokens数量: " + (response.getContext() != null ? response.getContext().size() : 0));
	}

}
