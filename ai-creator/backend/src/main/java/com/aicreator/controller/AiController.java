package com.aicreator.controller;

import com.aicreator.common.api.Result;
import com.aicreator.config.AiModelConfig;
import com.aicreator.dto.GenerateDTO;
import com.aicreator.security.JwtUser;
import com.aicreator.service.DeepSeekService;
import com.aicreator.sse.SseService;
import com.aicreator.strategy.TemplateContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI 生成控制器
 *
 * 核心接口：SSE 流式生成、模板列表查询
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final DeepSeekService deepSeekService;
    private final SseService sseService;
    private final TemplateContext templateContext;
    private final AiModelConfig aiModelConfig;

    /**
     * SSE 流式生成内容
     *
     * 客户端连接此接口后，服务端会持续推送数据片段
     * 前端使用 EventSource API 接收
     *
     * 返回类型是 SseEmitter，Spring MVC 会自动处理 SSE 协议
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 告诉浏览器这是 SSE 流
     */
    @GetMapping(value = "/generate/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateSSE(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "ARTICLE") String templateType,
            @RequestParam(required = false) String additionalPrompt,
            @RequestParam(required = false) String connectionId,
            @RequestParam(required = false) String model,
            @AuthenticationPrincipal JwtUser user) {

        log.info("SSE生成请求: keywords={}, template={}, user={}", keywords, templateType, user.getUsername());

        if (connectionId == null || connectionId.isBlank()) {
            connectionId = "sse-" + System.currentTimeMillis();
        }
        final String connId = connectionId;

        SseEmitter emitter = sseService.createEmitter(user.getUserId(), connId);

        CompletableFuture.runAsync(() -> {
            try {
                deepSeekService.generateStream(
                        keywords, templateType, additionalPrompt, model,
                        chunk -> sseService.send(user.getUserId(), connId, chunk)
                );
                sseService.complete(user.getUserId(), connId);
            } catch (Exception e) {
                log.error("AI生成失败", e);
                sseService.error(user.getUserId(), connId, e.getMessage());
            }
        });

        return emitter;
    }

    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@Valid @RequestBody GenerateDTO dto,
                                                 @AuthenticationPrincipal JwtUser user) {
        String result = deepSeekService.generate(dto.getKeywords(), dto.getTemplateType(), dto.getModelName());
        return Result.success(Map.of("content", result));
    }

    /**
     * 获取可用模型列表
     */
    @GetMapping("/models")
    public Result<List<Map<String, String>>> getModels() {
        List<Map<String, String>> models = aiModelConfig.getModels().stream()
                .map(m -> Map.of("name", m.getName(), "model", m.getModel()))
                .collect(Collectors.toList());
        return Result.success(models);
    }

    /**
     * 获取可用模板列表
     */
    @GetMapping("/templates")
    public Result<List<Map<String, String>>> getTemplates() {
        return Result.success(templateContext.getAllTypes());
    }
}
