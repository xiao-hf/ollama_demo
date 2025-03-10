package com.xiao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
} 