package com.aicreator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 生成请求 DTO
 */
@Data
public class GenerateDTO {
    @NotBlank(message = "关键词不能为空")
    private String keywords;           // 用户输入的关键词

    private String templateType;       // 模板类型：ARTICLE / SOCIAL_MEDIA / CODE / REPORT / TRANSLATION / CUSTOM
    private String title;              // 可选的标题
    private Integer maxTokens;         // 最大生成长度
    private Double temperature;        // 创意度（0~2）
    private String additionalPrompt;   // 额外的提示词
    private Long categoryId;           // 保存到的分类
    private String modelName;          // 模型名称（如 DeepSeek / 通义千问）
}
