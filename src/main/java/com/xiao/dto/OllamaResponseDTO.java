package com.xiao.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

/**
 * Ollama API响应DTO
 * 用于接收Ollama的生成结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OllamaResponseDTO {
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 创建时间
     * 使用JSONField注解指定日期格式
     */
    @JSONField(format = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
    private Date created_at;
    
    /**
     * 生成的回复内容
     */
    private String response;
    
    /**
     * 是否完成生成
     */
    private Boolean done;
    
    /**
     * 完成原因
     */
    private String done_reason;
    
    /**
     * 上下文信息
     */
    private List<Integer> context;
    
    /**
     * 总处理时间（以纳秒为单位）
     */
    private Long total_duration;
    
    /**
     * 加载模型的时间（以纳秒为单位）
     */
    private Long load_duration;
    
    /**
     * 提示处理时间（以纳秒为单位）
     */
    private Long prompt_eval_duration;
    
    /**
     * 生成回复的时间（以纳秒为单位）
     */
    private Long eval_duration;
    
    /**
     * 每秒生成的token数
     */
    private Double eval_count;
    
    /**
     * 提示中的token数
     */
    private Integer prompt_eval_count;
} 