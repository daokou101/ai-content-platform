package com.smarttask.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttask.config.DeepSeekConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * DeepSeek API 客户端
 *
 * 封装对 DeepSeek 大模型 API 的 HTTP 调用，支持流式（SSE）响应。
 *
 * DeepSeek 的 API 和 OpenAI 兼容，所以调用方式和 ChatGPT 一样。
 * API 文档：https://platform.deepseek.com/api-docs
 *
 * 流式调用原理：
 *   1. 发送 POST 请求时设置 stream=true
 *   2. DeepSeek 返回 HTTP 流式响应，每行格式为 data: {...}
 *   3. 用 BufferedReader 逐行读取，解析 JSON 提取 content
 *   4. 通过回调函数逐块返回给调用方
 *
 * 响应格式（SSE）：
 *   data: {"choices":[{"delta":{"role":"assistant","content":""},"index":0}]}
 *   data: {"choices":[{"delta":{"content":"你好"},"index":0}]}
 *   data: {"choices":[{"delta":{"content":"世界"},"index":0}]}
 *   data: [DONE]
 *
 * @RequiredArgsConstructor : Lombok 注解，为 deepSeekConfig 生成构造函数注入
 *   DeepSeekConfig 是从 application.yml 读取的配置（apiKey, apiUrl, timeout, model）
 *
 * 使用到的位置：
 *   - AiService.java → 调用 streamChat() 方法进行流式内容生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekClient {

    private final DeepSeekConfig deepSeekConfig;

    /** Jackson JSON 解析器，用于解析 DeepSeek 返回的流式 JSON */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * RestTemplate : Spring 的 HTTP 客户端
     * 这里没有用 @Bean 注入，而是直接 new 一个实例。
     * 因为 DeepSeekClient 只有一个，不需要全局配置 RestTemplate。
     *
     * 为什么不注入 WebClient？
     *   WebClient 对流式响应支持更好，但需要引入 spring-boot-starter-webflux。
     *   为了减少依赖，这里用 RestTemplate + BufferedReader 手动处理流式。
     *   这种方式更轻量，而且面试时能展示你对 HTTP 协议和流式处理的理解。
     */
    private RestTemplate restTemplate;

    /**
     * @PostConstruct : 在依赖注入完成后自动执行
     * 初始化 RestTemplate，设置超时时间
     */
    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
        // 设置连接超时和读取超时
        // SimpleClientHttpRequestFactory 是 RestTemplate 默认的底层实现
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);      // 连接超时 10 秒
        factory.setReadTimeout(deepSeekConfig.getTimeout()); // 读取超时从配置读取
        this.restTemplate.setRequestFactory(factory);
    }

    /**
     * 流式调用 DeepSeek Chat API
     *
     * 这是最核心的方法：建立 HTTP 连接后，逐行读取流式响应，
     * 通过回调函数将每个文本块返回给调用方。
     *
     * 回调函数说明：
     *   onChunk(String content)  : 每收到一个文本块时调用，content 是本次增量的文本
     *                              前端会将这些块逐个追加显示，形成"打字机效果"
     *   onComplete()              : 所有文本接收完毕时调用
     *   onError(Throwable ex)     : 发生错误时调用
     *
     * @param messages 消息列表，格式和 OpenAI API 一致：
     *                 [
     *                   {"role": "system", "content": "你是一位专业写手..."},
     *                   {"role": "user", "content": "请生成关于Java的文章..."}
     *                 ]
     * @param onChunk  文本块回调（会在流式响应的每个 data: {...} 行触发）
     * @param onComplete 完成回调（收到 [DONE] 或流结束时触发）
     * @param onError  错误回调（网络错误、API 错误等）
     */
    public void streamChat(List<Map<String, String>> messages,
                           Consumer<String> onChunk,
                           Runnable onComplete,
                           Consumer<Throwable> onError) {

        // 构建请求体
        // Map.of 是 Java 9+ 的便捷方法，创建不可变 Map
        // LinkedHashMap 保证字段顺序（虽然 JSON 不要求顺序，但方便调试）
        Map<String, Object> requestBody = new java.util.LinkedHashMap<>();
        requestBody.put("model", deepSeekConfig.getModel());     // 模型名称
        requestBody.put("messages", messages);                     // 对话消息
        requestBody.put("stream", true);                           // 启用流式输出
        requestBody.put("temperature", 0.7);                       // 温度参数（0-2），越高越有创造力

        try {
            String apiUrl = deepSeekConfig.getApiUrl() + "/v1/chat/completions";
            log.info("调用 DeepSeek API: model={}, messages={}", deepSeekConfig.getModel(), messages.size());

            // 使用 RestTemplate.execute(URI, HttpMethod, RequestCallback, ResponseExtractor) 方式
            // 为什么不直接用 RequestEntity？
            //   Spring Boot 3.2.x 的 RestTemplate.execute() 不支持直接传 RequestEntity，
            //   需要分别传 URI、HttpMethod、RequestCallback 和 ResponseExtractor。
            //   RequestCallback 负责设置请求头和请求体，
            //   ResponseExtractor 负责处理响应流。
            restTemplate.execute(
                    new java.net.URI(apiUrl),
                    org.springframework.http.HttpMethod.POST,

                    // RequestCallback: 设置请求头和请求体
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        request.getHeaders().setBearerAuth(deepSeekConfig.getApiKey());
                        request.getHeaders().setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                        // 写入请求体（JSON 序列化）
                        try (OutputStream os = request.getBody()) {
                            objectMapper.writeValue(os, requestBody);
                        }
                    },

                    // ResponseExtractor: 处理流式响应
                    response -> {
                        // 检查 HTTP 状态码
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            log.error("DeepSeek API 返回错误: status={}, body={}",
                                    response.getStatusCode(), errorBody);
                            onError.accept(new RuntimeException("DeepSeek API 错误: " + response.getStatusCode()));
                            return null;
                        }

                        // BufferedReader 逐行读取流式响应
                        // InputStreamReader 将字节流转换为字符流
                        // StandardCharsets.UTF_8 指定编码（DeepSeek 返回 UTF-8）
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {

                            String line;
                            while ((line = reader.readLine()) != null) {
                                // SSE 格式：每行以 "data: " 开头
                                if (line.startsWith("data: ")) {
                                    String data = line.substring(6).trim(); // 去掉 "data: " 前缀

                                    // 流结束标记
                                    if ("[DONE]".equals(data)) {
                                        log.debug("DeepSeek 流式响应结束");
                                        onComplete.run();
                                        return null;
                                    }

                                    // 解析 JSON，提取增量内容
                                    try {
                                        JsonNode json = objectMapper.readTree(data);
                                        // JSONPath: /choices/0/delta/content
                                        // choices 数组的第一个元素的 delta 对象的 content 字段
                                        // 使用 at() 方法支持 JSONPath 表达式
                                        JsonNode deltaContent = json.at("/choices/0/delta/content");

                                        if (deltaContent != null && !deltaContent.isNull()) {
                                            String content = deltaContent.asText();
                                            if (!content.isEmpty()) {
                                                onChunk.accept(content);
                                            }
                                        }
                                    } catch (Exception e) {
                                        // 偶尔会有格式异常的行，跳过即可
                                        log.warn("解析 DeepSeek 响应行失败: {}", data);
                                    }
                                }
                            }

                            // 流正常结束（没有 [DONE] 标记但读取完毕）
                            onComplete.run();
                        } catch (Exception e) {
                            log.error("读取 DeepSeek 流式响应失败", e);
                            onError.accept(e);
                        }

                        return null;
                    }
            );

        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            onError.accept(e);
        }
    }
}
