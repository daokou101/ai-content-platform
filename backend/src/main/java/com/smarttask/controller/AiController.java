package com.smarttask.controller;

import com.smarttask.common.api.Result;
import com.smarttask.entity.AiModel;
import com.smarttask.entity.AiTemplate;
import com.smarttask.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI 内容生成控制器
 *
 * 提供 AI 内容生成相关的三个接口：
 *   1. 获取 AI 模板列表（供前端模板选择器使用）
 *   2. 获取 AI 模型列表（供前端模型下拉框使用）
 *   3. SSE 流式生成内容（核心接口，逐字返回生成结果）
 *
 * @RestController : @Controller + @ResponseBody 的组合注解
 *   所有方法的返回值自动序列化为 JSON 写入 HTTP 响应体
 * @RequestMapping("/api/ai") : 所有接口的路径前缀
 *   - GET  /api/ai/templates     → 模板列表（页面加载时调用）
 *   - GET  /api/ai/models        → 模型列表（页面加载时调用）
 *   - GET  /api/ai/generate/sse  → 流式生成（EventSource 连接）
 *
 * @RequiredArgsConstructor : Lombok 注解
 *   为 final 字段 aiService 和 taskExecutor 生成构造函数注入
 *
 * SSE 接口认证说明：
 *   由于前端使用 EventSource 调用 SSE 接口，而 EventSource 无法设置自定义请求头，
 *   JWT token 通过 URL 查询参数传递：GET /api/ai/generate/sse?keywords=xxx&templateType=xxx&token=xxx
 *   详见 JwtAuthenticationFilter 中对 SSE 路径的处理
 *
 * 使用到的位置：
 *   前端 Generate.vue → 调用这三个接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    /** AI 业务服务，处理模板/模型查询和生成编排 */
    private final AiService aiService;

    /**
     * 异步任务线程池（bean 名称为 "taskExecutor"）
     *
     * 使用 @Qualifier("taskExecutor") 指定注入 ThreadPoolConfig 中定义的 taskExecutor Bean。
     * 为什么需要单独的线程池？
     *   因为 SseEmitter 必须在 controller 方法返回前创建并返回给前端，
     *   但 AI 生成是耗时的流式操作（可能持续几十秒），不能阻塞 controller 的线程。
     *   所以 controller 先创建 SseEmitter，然后用线程池另起线程执行 AI 生成，
     *   controller 立即返回，Tomcat 线程得到释放。
     *
     * 如果不这样做：
     *   Tomcat 的线程会被长时间占用，高并发时线程池会耗尽，其他请求无法处理。
     */
    private final ThreadPoolTaskExecutor taskExecutor;

    /**
     * 获取所有 AI 模板列表
     *
     * 前端调用时机：
     *   Generate.vue → onMounted() → getTemplates()
     *   返回的模板数据用于渲染模板选择卡片（文章写作、营销文案等）
     *
     * 数据来源：ai_template 表，按 sort_order 升序排列
     *
     * 返回示例：
     *   [
     *     {"type": "article", "name": "文章写作", "description": "生成高质量的文章内容"},
     *     {"type": "marketing", "name": "营销文案", "description": "生成营销推广文案"},
     *     ...
     *   ]
     *
     * @return 模板列表（已排序）
     */
    @GetMapping("/templates")
    public Result<List<AiTemplate>> getTemplates() {
        return Result.success(aiService.getTemplates());
    }

    /**
     * 获取所有 AI 模型列表
     *
     * 前端调用时机：
     *   Generate.vue → onMounted() → getModels()
     *   返回的模型数据用于渲染模型选择下拉框
     *
     * 数据来源：ai_model 表，按 sort_order 升序排列
     *
     * 返回示例：
     *   [
     *     {"name": "DeepSeek Chat", "model": "deepseek-chat", "provider": "deepseek"},
     *     {"name": "DeepSeek Reasoner", "model": "deepseek-reasoner", "provider": "deepseek"}
     *   ]
     *
     * @return 模型列表（已排序）
     */
    @GetMapping("/models")
    public Result<List<AiModel>> getModels() {
        return Result.success(aiService.getModels());
    }

    /**
     * SSE 流式生成内容（核心接口）
     *
     * 这是整个 AI 平台最关键的方法。
     * 前端使用 EventSource 建立 SSE 连接，后端通过 SseEmitter 逐字推送生成结果。
     *
     * 调用流程：
     *   Generate.vue 点击"开始生成"按钮
     *     → new EventSource('/api/ai/generate/sse?keywords=xxx&templateType=xxx&token=xxx')
     *       → AiController.generateStream() 建立 SSE 连接
     *         → 创建 SseEmitter（超时时间从配置读取，默认 5 分钟）
     *         → 使用 taskExecutor 异步执行 aiService.generateStream()
     *         → controller 立即返回 SseEmitter（不阻塞 Tomcat 线程）
     *           → DeepSeek API 逐块返回 → AiService 逐字转发 → SseEmitter 逐字推送
     *             → 前端逐字显示（打字机效果）
     *
     * 为什么用 GET 而不是 POST？
     *   EventSource 规范只支持 GET 请求，不支持 POST，也不能设置请求头。
     *   所以参数通过 URL 查询参数传递，JWT token 也通过查询参数传递。
     *
     * 超时说明：
     *   SseEmitter 默认超时是 30 秒（Spring Boot 默认），这里设置为 300000ms（5 分钟），
     *   因为 AI 生成大段文本可能需要 1-2 分钟。超时后 SS E 连接会自动关闭。
     *
     * @param keywords         用户输入的关键词（必填）
     * @param templateType     模板类型（必填），如 "article"、"marketing" 等
     * @param additionalPrompt 用户额外提示（可选），如"语气轻松一点"
     * @param model            模型标识（可选），如 "deepseek-chat"，为空则使用模板默认
     * @return SseEmitter 对象，Spring 会自动处理 SSE 连接的建立和数据推送
     */
    @GetMapping("/generate/sse")
    public SseEmitter generateStream(
            @RequestParam String keywords,
            @RequestParam String templateType,
            @RequestParam(required = false) String additionalPrompt,
            @RequestParam(required = false) String model) {

        // ========== 1. 创建 SseEmitter ==========
        // 参数：超时时间（毫秒），0 表示永不超时
        // 这里设置 300000ms（5 分钟），因为 AI 生成长文本可能需要较长时间
        // 如果用户在生成过程中关闭页面，Spring 会自动检测到 SseEmitter 已完成并清理
        SseEmitter emitter = new SseEmitter(300000L);

        log.info("创建 SSE 连接: keywords={}, templateType={}", keywords, templateType);

        // ========== 2. 异步执行 AI 生成 ==========
        // 使用 taskExecutor 线程池异步执行，避免阻塞 Tomcat 的请求处理线程
        // 如果直接在当前线程中执行 AI 生成，Tomcat 的线程会被长时间占用
        // 高并发场景下可能导致 Tomcat 线程池耗尽
        taskExecutor.execute(() -> {
            try {
                aiService.generateStream(keywords, templateType, additionalPrompt, model, emitter);
            } catch (Exception e) {
                log.error("AI 生成流执行异常", e);
                try {
                    emitter.send(SseEmitter.event().name("error")
                            .data(Map.of("content", "生成服务异常，请稍后重试")));
                    emitter.complete();
                } catch (Exception ex) {
                    // ignore
                }
            }
        });

        // ========== 3. 立即返回 SseEmitter ==========
        // controller 方法返回后，Tomcat 线程释放，
        // SSE 连接由 Spring 的异步机制保持（底层使用 Servlet 3.0 异步支持）
        // 后续数据通过另一个线程写入 SseEmitter
        return emitter;
    }
}
