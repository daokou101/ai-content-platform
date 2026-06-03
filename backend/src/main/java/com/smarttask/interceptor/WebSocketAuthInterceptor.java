package com.smarttask.interceptor;

import com.smarttask.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 *
 * HandshakeInterceptor: Spring WebSocket 的拦截器接口
 * 在 WebSocket 建立连接之前（beforeHandshake）和之后（afterHandshake）执行
 *
 * 这里用来验证 WebSocket 连接的 Token 鉴权
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            // 从 URL 参数中获取 Token
            String token = httpRequest.getParameter("token");
            if (token != null && jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                attributes.put("userId", userId);
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后无需额外处理
    }
}
