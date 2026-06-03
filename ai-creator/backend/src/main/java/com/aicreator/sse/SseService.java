package com.aicreator.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE（Server-Sent Events）服务
 *
 * SSE 是 HTML5 标准，允许服务端主动向客户端推送数据
 * 和 WebSocket 的区别：
 *   - SSE：单向（服务端→客户端），基于 HTTP，浏览器原生支持 EventSource API
 *   - WebSocket：双向，需要单独协议握手
 *
 * AI 流式生成场景 SSE 比 WebSocket 更合适：
 *   1. 我们只需要服务端→客户端的单向推送
 *   2. SSE 直接用 HTTP，不需要额外协议
 *   3. 前端用 EventSource API 即可接收，代码极简
 *
 * 每个用户可以有多个 SSE 连接（不同浏览器标签页）
 */
@Slf4j
@Service
public class SseService {

    /** 用户的 SSE 发射器集合（key: userId, value: 发射器Map） */
    private final Map<Long, Map<String, SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /**
     * 创建 SSE 连接
     * @param userId 用户ID
     * @param connectionId 连接标识（用于区分同一用户的不同标签页）
     * @return SseEmitter
     *
     * SseEmitter: Spring MVC 提供的 SSE 发射器
     *   通过它可以直接向客户端推送数据
     *   timeout 设为 0 表示不超时（AI 生成可能比较久）
     */
    public SseEmitter createEmitter(Long userId, String connectionId) {
        // 超时时间设为 10 分钟，AI 长文本生成可能需要较长时间
        SseEmitter emitter = new SseEmitter(600000L);

        userEmitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(connectionId, emitter);

        emitter.onCompletion(() -> remove(userId, connectionId));
        emitter.onTimeout(() -> remove(userId, connectionId));
        emitter.onError(e -> remove(userId, connectionId));

        log.info("SSE 连接创建: userId={}, connectionId={}", userId, connectionId);
        return emitter;
    }

    /**
     * 向指定用户推送数据
     */
    public void send(Long userId, String connectionId, Object data) {
        Map<String, SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        SseEmitter emitter = emitters.get(connectionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(data));
            } catch (IOException e) {
                remove(userId, connectionId);
            }
        }
    }

    /**
     * 向指定用户发送完成信号
     */
    public void complete(Long userId, String connectionId) {
        Map<String, SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null) return;
        SseEmitter emitter = emitters.get(connectionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            } catch (IOException ignored) {}
            emitter.complete();
            remove(userId, connectionId);
        }
    }

    /**
     * 向指定用户发送错误信息
     */
    public void error(Long userId, String connectionId, String errorMessage) {
        Map<String, SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null) return;
        SseEmitter emitter = emitters.get(connectionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("error").data(errorMessage));
            } catch (IOException ignored) {}
            emitter.completeWithError(new Throwable(errorMessage));
            remove(userId, connectionId);
        }
    }

    private void remove(Long userId, String connectionId) {
        Map<String, SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(connectionId);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }
}
