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
				.stream(false)            // 不使用流式响应
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
	
	@Test
	void testStreamAsk() throws InterruptedException {
		StringBuilder fullResponse = new StringBuilder();
		CountDownLatch latch = new CountDownLatch(1);
		
		// 请求模型并流式处理响应
		ollamaStreamUtil.streamRequest(
				"deepseek-r1:8b",    // 模型名称
				"请详细介绍一下中国的四大名著",  // 提示文本
				
				// 接收每个响应片段的处理函数
				chunk -> {
					System.out.print(chunk); // 实时打印每个片段
					fullResponse.append(chunk);
				},
				
				// 完成时的回调函数
				() -> {
					System.out.println("\n\n===== 响应完成 =====");
					System.out.println("完整响应长度: " + fullResponse.length() + " 字符");
					latch.countDown();
				},
				
				// 错误处理函数
				error -> {
					System.err.println("发生错误: " + error.getMessage());
					latch.countDown();
				}
		);
		
		// 等待响应完成（最多等待2分钟）
		latch.await(2, TimeUnit.MINUTES);
	}
	
	@Test
	void testStreamAskCharByChar() throws InterruptedException {
		StringBuilder fullResponse = new StringBuilder();
		CountDownLatch latch = new CountDownLatch(1);
		
		System.out.println("开始打字机效果输出...\n");
		
		// 请求模型并以字符级别流式处理响应（打字机效果）
		ollamaStreamUtil.streamRequestCharByChar(
				"deepseek-r1:8b",    // 模型名称
				"请简单介绍一下Java语言的特点",  // 提示文本
				
				// 接收每个字符的处理函数
				character -> {
					System.out.print(character); // 实时打印每个字符
					fullResponse.append(character);
				},
				
				// 完成时的回调函数
				() -> {
					System.out.println("\n\n===== 响应完成 =====");
					System.out.println("完整响应长度: " + fullResponse.length() + " 字符");
					latch.countDown();
				},
				
				// 错误处理函数
				error -> {
					System.err.println("发生错误: " + error.getMessage());
					latch.countDown();
				},
				
				0  // 字符间延迟（毫秒），设置为0可以取消延迟
		);
		
		// 等待响应完成（最多等待5分钟）
		latch.await(5, TimeUnit.MINUTES);
	}

	@Test
	void contextLoads() {
		System.out.println(userMapper.selectByPrimaryKey(1L));
	}
	
	@Test
	void printTodayDate() {
		LocalDate today = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
		String formattedDate = today.format(formatter);
		System.out.println("今天的日期是: " + formattedDate);
	}

}
