package com.xiao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Ollama API请求DTO
 * 用于向Ollama发送生成请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OllamaRequestDTO {
    
    /**
     * 模型名称，例如："llama2", "mistral", "gemma"等
     */
    private String model;
    
    /**
     * 提示文本/用户问题
     */
    private String prompt;
    
    /**
     * 是否流式返回结果
     * 默认为false
     */
    @Builder.Default
    private Boolean stream = false;
    
    /**
     * 上下文信息，用于保持对话连续性
     * 这是上一次响应中返回的context数组
     */
    private List<Integer> context;
    
    /**
     * 模型参数选项
     */
    private Options options;
    
    /**
     * 系统提示，用于设置模型的行为
     */
    private String system;
    
    /**
     * 模型参数选项内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        /**
         * 温度参数，控制随机性，范围0-1
         * 较高的值（如0.8）会使输出更加随机，
         * 较低的值（如0.2）会使输出更加确定和集中
         */
        @Builder.Default
        private Float temperature = 0.7f;
        
        /**
         * 控制输出的随机性，范围0-1
         * 仅考虑累积概率达到top_p的tokens
         */
        private Float top_p;
        
        /**
         * 每个生成步骤要考虑的最高概率token数
         */
        private Integer top_k;
        
        /**
         * 最大生成的token数量
         */
        @Builder.Default
        private Integer num_predict = 128;
        
        /**
         * 停止生成的字符串列表
         */
        private String[] stop;
        
        /**
         * 重复惩罚，避免重复，值越大惩罚越强
         */
        private Float repeat_penalty;
        
        /**
         * 是否保持上下文
         */
        private Boolean keep_alive;
    }
} 