package com.smarttask.dto;

import lombok.Data;

/**
 * AI 内容生成请求 DTO
 *
 * 前端在 AI 生成页面点击"开始生成"时，将配置参数封装成此对象发送给后端。
 *
 * 使用到的位置（在 AiController 和 AiService 中）：
 *   - POST /api/ai/generate → 同步生成请求
 *   - AiService.generateStream() → 解析参数并构建 AI 请求
 *
 * 属性说明：
 *   keywords         - 生成关键词，用户输入的描述性文字，多个用逗号分隔
 *                      例如："Java Spring Boot 微服务 入门教程"
 *   templateType     - 模板类型，决定 AI 使用什么风格生成
 *                      取值范围：article / marketing / social / seo / summary
 *   additionalPrompt - 额外的提示要求，用户可输入的补充说明，如"语气轻松一点"
 *   model            - 使用的模型标识，如 "deepseek-chat"，为空时使用默认模型
 */
@Data
public class GenerateRequest {

    private String keywords;

    private String templateType;

    private String additionalPrompt;

    private String model;
}
