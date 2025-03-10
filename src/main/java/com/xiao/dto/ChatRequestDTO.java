package com.xiao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 聊天请求DTO
 * 用于接收前端发送的聊天请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    
    /**
     * 用户输入的文本
     */
    private String text;
    
    /**
     * 模型名称，默认为deepseek-r1:8b
     */
    private String model = "deepseek-r1:8b";
    
    /**
     * 上下文信息，用于保持对话连续性
     * 这是上一次响应中返回的context数组
     */
    private List<Integer> context;
    
    /**
     * 提供两参数的构造函数，为了兼容旧代码
     */
    public ChatRequestDTO(String text, String model) {
        this.text = text;
        this.model = model;
    }
} 