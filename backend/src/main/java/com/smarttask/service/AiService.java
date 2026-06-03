package com.smarttask.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarttask.entity.AiModel;
import com.smarttask.entity.AiTemplate;
import com.smarttask.mapper.AiModelMapper;
import com.smarttask.mapper.AiTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 内容生成服务
 *
 * 负责 AI 内容生成的核心业务逻辑：
 *   1. 提供模板和模型列表（供前端下拉框展示）
 *   2. 构建 AI 提示词（system_prompt + 用户输入）
 *   3. 协调 DeepSeekClient 的流式调用
 *   4. 将生成结果逐字转发给前端的 SseEmitter
 *
 * 调用链路：
 *   ＡｉＣｏｎｔｒｏｌｌｅｒ（接收 SSE 请求）
 *     → ＡｉＳｅｒｖｉｃｅ（编排业务逻辑）
 *       → ＤｅｅｐＳｅｅｋＣｌｉｅｎｔ（调用 AI API）
 *         → 回调逐块返回
 *       → ＳｓｅＥｍｉｔｔｅｒ（推送给前端）
 *
 * @RequiredArgsConstructor : Lombok 注解，为所有 final 字段生成构造函数注入
 *   注入 AiTemplateMapper、AiModelMapper、DeepSeekClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiTemplateMapper aiTemplateMapper;
    private final AiModelMapper aiModelMapper;
    private final DeepSeekClient deepSeekClient;

    /**
     * 获取所有 AI 模板列表
     *
     * 数据来源：ai_template 表（在 init.sql 中初始化）
     * 模板包含：文章写作、营销文案、社交媒体、SEO优化、内容摘要
     *
     * 前端调用：Generate.vue 的 onMounted() 中调用 getTemplates() 获取模板列表
     * 返回数据示例：
     *   [
     *     {"type": "article", "name": "文章写作", "description": "生成高质量的文章内容"},
     *     {"type": "marketing", "name": "营销文案", "description": "生成营销推广文案"},
     *     ...
     *   ]
     *
     * @return 按 sortOrder 排序的模板列表
     */
    public List<AiTemplate> getTemplates() {
        // LambdaQueryWrapper 构造排序条件
        // orderByAsc(AiTemplate::getSortOrder) → ORDER BY sort_order ASC
        LambdaQueryWrapper<AiTemplate> wrapper = new LambdaQueryWrapper<AiTemplate>()
                .orderByAsc(AiTemplate::getSortOrder);
        return aiTemplateMapper.selectList(wrapper);
    }

    /**
     * 获取所有 AI 模型列表
     *
     * 数据来源：ai_model 表（在 init.sql 中初始化）
     * 模型包含：DeepSeek Chat、DeepSeek Reasoner
     *
     * 前端调用：Generate.vue 的 onMounted() 中调用 getModels() 获取模型列表
     * 返回数据示例：
     *   [
     *     {"name": "DeepSeek Chat", "model": "deepseek-chat", "provider": "deepseek"},
     *     {"name": "DeepSeek Reasoner", "model": "deepseek-reasoner", "provider": "deepseek"}
     *   ]
     *
     * @return 按 sortOrder 排序的模型列表
     */
    public List<AiModel> getModels() {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<AiModel>()
                .orderByAsc(AiModel::getSortOrder);
        return aiModelMapper.selectList(wrapper);
    }

    /**
     * 流式生成 AI 内容
     *
     * 核心方法：将前端传的参数转为 DeepSeek 的消息格式，
     * 调用 DeepSeekClient.streamChat() 获取流式响应，
     * 将每个文本块通过 SseEmitter 推送给前端。
     *
     * 这个方法在 AiController 的 SSE 端点中被异步调用（使用 taskExecutor 线程池）。
     * controller 先创建 SseEmitter 返回给前端，然后在这个方法中往 emitter 写数据。
     *
     * @param keywords         用户输入的关键词，例如 "Java Spring Boot 微服务"
     * @param templateType     模板类型，例如 "article"（用于查找对应的 system_prompt）
     * @param additionalPrompt 用户额外提示，例如 "语气轻松一点"，可为 null
     * @param model            模型标识，例如 "deepseek-chat"，为空则使用模板默认
     * @param emitter          SseEmitter 对象，通过它向前端推送 SSE 事件
     *
     * SSE 事件格式：
     *   逐字推送：event: message, data: {"content": "你"}
     *   逐字推送：event: message, data: {"content": "好"}
     *   生成完成：event: done, data: ""
     *   发生错误：event: error, data: "错误信息"
     */
    public void generateStream(String keywords,
                               String templateType,
                               String additionalPrompt,
                               String model,
                               SseEmitter emitter) {
        try {
            // ========== 1. 构建消息列表 ==========
            // 消息列表包含两个部分：
            //   system message → 模板对应的系统提示词（设定 AI 的角色和风格）
            //   user message   → 用户输入的关键词和额外要求
            List<Map<String, String>> messages = buildMessages(keywords, templateType, additionalPrompt);

            if (messages.isEmpty()) {
                emitter.send(SseEmitter.event().name("error").data("模板不存在或参数错误"));
                emitter.complete();
                return;
            }

            // ========== 2. 调用 DeepSeek 流式 API ==========
            // 三个回调函数：
            //   onChunk    → 收到一个文本块 → 通过 SSE 推给前端
            //   onComplete → 生成完成 → 发送 done 事件 → 关闭连接
            //   onError    → 生成失败 → 发送 error 事件 → 关闭连接
            deepSeekClient.streamChat(
                    messages,

                    // onChunk: 收到文本块时的回调
                    // 每个块都是一个文本片段，直接追加到 SSE 事件的 data 字段中
                    chunk -> {
                        try {
                            // SseEmitter.event() 创建 SSE 事件
                            // .name("message") 设置事件名称（前端监听 message 事件）
                            // .data() 设置事件数据（必须是一个可序列化的对象）
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(Map.of("content", chunk)));
                        } catch (IOException e) {
                            // 客户端断开连接时忽略
                            log.warn("SSE 发送失败，客户端可能已断开: {}", e.getMessage());
                        }
                    },

                    // onComplete: 生成完成时的回调
                    () -> {
                        try {
                            // 发送 done 事件通知前端生成完毕
                            emitter.send(SseEmitter.event().name("done").data(""));
                            emitter.complete();
                        } catch (IOException e) {
                            log.warn("SSE 完成事件发送失败: {}", e.getMessage());
                        }
                    },

                    // onError: 发生错误时的回调
                    error -> {
                        try {
                            emitter.send(SseEmitter.event().name("error")
                                    .data(error.getMessage() != null ? error.getMessage() : "生成失败"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.error("SSE 错误事件发送失败", e);
                        }
                    }
            );

        } catch (Exception e) {
            log.error("AI 生成失败", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("生成失败"));
                emitter.complete();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * 构建 DeepSeek API 的消息列表
     *
     * 将模板的 system_prompt 和用户的输入组合成 AI 能理解的消息格式。
     *
     * OpenAI/DeepSeek 的消息格式：
     *   [
     *     {"role": "system", "content": "你是一个专业写手，请生成文章..."},
     *     {"role": "user", "content": "关键词：Java Spring Boot\n额外要求：语气轻松"}
     *   ]
     *
     * system message : 设定 AI 的行为和角色，影响整个对话的风格
     * user message   : 用户的具体需求，AI 根据这个生成内容
     *
     * @param keywords         用户输入的关键词
     * @param templateType     模板类型（用于查找 system_prompt）
     * @param additionalPrompt 用户额外提示（可选）
     * @return 消息列表，供 DeepSeek API 使用
     */
    private List<Map<String, String>> buildMessages(String keywords, String templateType,
                                                     String additionalPrompt) {
        // 根据模板类型从数据库查找对应的 system_prompt
        LambdaQueryWrapper<AiTemplate> wrapper = new LambdaQueryWrapper<AiTemplate>()
                .eq(AiTemplate::getType, templateType);
        AiTemplate template = aiTemplateMapper.selectOne(wrapper);

        if (template == null) {
            log.warn("模板不存在: {}", templateType);
            return List.of();
        }

        List<Map<String, String>> messages = new ArrayList<>();

        // 1. System Message: 定义 AI 的角色和行为
        // 来自 ai_template 表的 system_prompt 字段
        // 例如："你是一位专业的文章写手。请根据用户提供的关键词和提示..."
        Map<String, String> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", template.getSystemPrompt());
        messages.add(systemMessage);

        // 2. User Message: 用户的输入内容
        // 组合关键词和额外提示，让 AI 知道要生成什么
        StringBuilder userContent = new StringBuilder();
        userContent.append("请根据以下关键词生成内容：").append(keywords);

        if (additionalPrompt != null && !additionalPrompt.trim().isEmpty()) {
            userContent.append("\n\n额外要求：").append(additionalPrompt.trim());
        }

        userContent.append("\n\n请用中文回答。");

        Map<String, String> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent.toString());
        messages.add(userMessage);

        return messages;
    }
}
