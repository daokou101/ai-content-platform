package com.smarttask.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 配置类
 *
 * 从 application.yml 中读取 deepseek 开头的配置项。
 * 使用 @ConfigurationProperties 批量注入，比 @Value 一个个写更优雅。
 *
 * @ConfigurationProperties(prefix = "deepseek") :
 *   将配置文件中 deepseek.api-key → 注入 apiKey 字段
 *   将配置文件中 deepseek.api-url  → 注入 apiUrl 字段
 *   将配置文件中 deepseek.timeout   → 注入 timeout 字段
 *   将配置文件中 deepseek.model     → 注入 model 字段
 *
 * @Component :
 *   将该类注册为 Spring Bean，@ConfigurationProperties 需要在 Spring 容器中才能生效
 *
 * 使用到的位置：
 *   - DeepSeekClient.java → 调用 API 时读取 apiKey、apiUrl、model
 *   - AiController.java → 指定默认模型时读取 model
 *
 * application.yml 中的配置示例：
 * ```yaml
 * deepseek:
 *   api-key: sk-your-key-here
 *   api-url: https://api.deepseek.com
 *   timeout: 60000
 *   model: deepseek-chat
 * ```
 *
 * 环境变量方式（推荐，避免把 key 提交到 git）：
 *   在 docker-compose.yml 或系统环境中设置 DEEPSEEK_API_KEY
 *   Spring Boot 会自动将环境变量映射到 api-key（宽松绑定规则）
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    /**
     * DeepSeek API 密钥
     * 从 https://platform.deepseek.com/ 注册获取
     * 建议通过环境变量 DEEPSEEK_API_KEY 设置，不要硬编码在配置文件中
     */
    private String apiKey;

    /**
     * DeepSeek API 基础地址
     * 默认值在 application.yml 中设置：https://api.deepseek.com
     */
    private String apiUrl;

    /**
     * HTTP 请求超时时间（毫秒）
     * 默认 60000ms（60 秒），因为 AI 生成可能需要较长时间
     */
    private int timeout = 60000;

    /**
     * 默认使用的模型标识
     * 默认值在 application.yml 中设置：deepseek-chat
     */
    private String model;
}
