package com.smarttask.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 通知处理器
 *
 * TextWebSocketHandler: Spring WebSocket 提供的文本消息处理器
 * 负责处理 WebSocket 连接的建立、消息接收、连接关闭等生命周期
 *
 * 核心数据结构：
 *   sessions: ConcurrentHashMap，存储所有在线的 WebSocket 连接
 *   key = 用户ID, value = WebSocketSession
 *   使用 ConcurrentHashMap 保证多线程安全
 *
 * 当有通知需要推送给用户时，从此 Map 中找到用户的 WebSocket 连接，直接发送消息
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
//继承 TextWebSocketHandler：Spring 提供的文本消息处理器，专门处理 WebSocket 文本通信
    /** 在线用户的 WebSocket 会话集合（线程安全）  存储 【用户ID → 用户的WebSocket连接会话】 */
    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 连接建立后的回调
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从连接属性中拿到用户ID（拦截器里存进去的）
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.put(userId, session);
            log.info("用户 {} 已建立 WebSocket 连接", userId);
        }
    }

    /**
     * 收到客户端消息时的回调
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 目前不做客户端消息处理，只做服务端推送
        log.debug("收到 WebSocket 消息: {}", message.getPayload());
    }

    /**
     * 连接关闭后的回调
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            log.info("用户 {} 已断开 WebSocket 连接", userId);
        }
    }

    /**
     * 向指定用户推送通知
     * @return true=推送成功, false=用户不在线
     */
    public boolean sendToUser(Long userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (IOException e) {
                log.error("向用户 {} 推送消息失败", userId, e);
                sessions.remove(userId);
            }
        }
        return false;
    }

    /**
     * 获取当前在线用户数
     */
    public int getOnlineCount() {
        return sessions.size();
    }
}
