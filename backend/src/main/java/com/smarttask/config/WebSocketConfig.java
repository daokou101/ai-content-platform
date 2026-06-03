package com.smarttask.config;

import com.smarttask.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket 配置类
 *
 * WebSocket: 一种在单个 TCP 连接上实现全双工通信的协议
 * 不同于 HTTP 的"请求-响应"模式，WebSocket 允许服务端主动推送消息到客户端
 * 适用于：实时通知、即时消息、数据看板实时刷新等场景
 *
 * @EnableWebSocket: 启用 Spring WebSocket 支持
 *
 * 这里我们将 WebSocket 路径注册为 /ws/{userId}
 * 客户端连接时需要在路径中带上用户ID，用于鉴权和定向推送
 */
@Configuration
@EnableWebSocket
//核心注解：开启 WebSocket 功能（不加这个注解，WebSocket 完全用不了）
@RequiredArgsConstructor
//实现 WebSocketConfigurer 接口：自定义 WebSocket 配置
public class WebSocketConfig implements WebSocketConfigurer {
    //【WebSocket 处理器】：真正处理「连接、收发消息、断开连接」的核心类
    private final TextWebSocketHandler notificationWebSocketHandler;
    //WebSocket 拦截器：连接前做**用户登录校验**，防止非法连接
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    /**
     * 注册 WebSocket 连接规则（核心方法）
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 绑定处理器 + 设置连接地址：/ws/{userId}
        registry.addHandler(notificationWebSocketHandler, "/ws/{userId}")
                // 添加鉴权拦截器：连接前验证用户是否登录
                .addInterceptors(webSocketAuthInterceptor)
                // 允许跨域：所有前端域名都能连接（开发环境常用）
                .setAllowedOrigins("*");
    }
}
