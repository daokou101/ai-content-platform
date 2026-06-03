package com.aicreator.service;

import com.aicreator.common.api.ResultCode;
import com.aicreator.common.exception.BusinessException;
import com.aicreator.config.AiModelConfig;
import com.aicreator.strategy.TemplateContext;
import com.aicreator.strategy.TemplateStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AI 服务
 *
 * 支持多个模型供应商（DeepSeek、通义千问、智谱 GLM 等），
 * 全部通过 OpenAI 兼容格式调用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final TemplateContext templateContext;
    private final AiModelConfig aiModelConfig;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.max-tokens}")
    private int maxTokens;

    @Value("${deepseek.temperature}")
    private double temperature;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // ===== 流式生成（支持指定模型） =====

    public String generateStream(String keywords, String templateType, String additionalPrompt,
                                  Consumer<String> onChunk) {
        return generateStream(keywords, templateType, additionalPrompt, null, onChunk);
    }

    public String generateStream(String keywords, String templateType, String additionalPrompt,
                                  String modelName, Consumer<String> onChunk) {
        AiModelConfig.ModelInfo modelInfo = resolveModel(modelName);

        TemplateStrategy strategy = templateContext.getStrategy(templateType);
        String systemPrompt = strategy.buildSystemPrompt(keywords);
        String userMessage = strategy.buildUserMessage(keywords, additionalPrompt);

        String requestBody = buildRequestBody(systemPrompt, userMessage, true, modelInfo.getModel());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(modelInfo.getApiUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "text/event-stream")
                .post(RequestBody.create(requestBody, JSON))
                .build();

        StringBuilder fullContent = new StringBuilder();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI服务返回状态码: " + response.code());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) break;

                    String content = parseContentDelta(data);
                    if (content != null && !content.isEmpty()) {
                        fullContent.append(content);
                        onChunk.accept(content);
                    }
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI服务调用失败: " + e.getMessage());
        }

        return fullContent.toString();
    }

    // ===== 非流式生成（支持指定模型） =====

    public String generate(String keywords, String templateType) {
        return generate(keywords, templateType, null);
    }

    public String generate(String keywords, String templateType, String modelName) {
        AiModelConfig.ModelInfo modelInfo = resolveModel(modelName);

        TemplateStrategy strategy = templateContext.getStrategy(templateType);
        String requestBody = buildRequestBody(
                strategy.buildSystemPrompt(keywords),
                strategy.buildUserMessage(keywords, null),
                false, modelInfo.getModel());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(modelInfo.getApiUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
            }
            String json = response.body().string();
            return extractContent(json);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI服务调用失败");
        }
    }

    // ===== 私有方法 =====

    private AiModelConfig.ModelInfo resolveModel(String modelName) {
        if (modelName != null && !modelName.isBlank()) {
            AiModelConfig.ModelInfo info = aiModelConfig.getModelByName(modelName);
            if (info != null) return info;
            log.warn("未找到模型 '{}'，使用默认模型", modelName);
        }
        return aiModelConfig.getDefaultModelInfo();
    }

    private String buildRequestBody(String systemPrompt, String userMessage, boolean stream, String modelName) {
        String escapedSystem = systemPrompt.replace("\\", "\\\\").replace("\"", "\\\"");
        String escapedUser = userMessage.replace("\\", "\\\\").replace("\"", "\\\"");

        return "{"
                + "\"model\": \"" + modelName + "\","
                + "\"stream\": " + stream + ","
                + "\"max_tokens\": " + maxTokens + ","
                + "\"temperature\": " + temperature + ","
                + "\"messages\": ["
                + "{\"role\": \"system\", \"content\": \"" + escapedSystem + "\"},"
                + "{\"role\": \"user\", \"content\": \"" + escapedUser + "\"}"
                + "]"
                + "}";
    }

    private String parseContentDelta(String data) {
        try {
            int deltaIndex = data.indexOf("\"delta\"");
            if (deltaIndex < 0) return null;
            int contentIndex = data.indexOf("\"content\"", deltaIndex);
            if (contentIndex < 0) return null;
            int start = data.indexOf("\"", contentIndex + 10);
            if (start < 0) return null;
            int end = data.indexOf("\"", start + 1);
            if (end < 0) return null;
            return data.substring(start + 1, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractContent(String json) {
        try {
            int choicesStart = json.indexOf("\"choices\"");
            if (choicesStart < 0) return null;
            int contentIndex = json.indexOf("\"content\"", choicesStart);
            if (contentIndex < 0) return null;
            int start = json.indexOf("\"", contentIndex + 10);
            if (start < 0) return null;
            int end = json.lastIndexOf("\"", json.length() - 2);
            if (end < 0 || end <= start) return null;
            return json.substring(start + 1, end);
        } catch (Exception e) {
            return null;
        }
    }
}
