package com.smarttask.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * HTTP 请求上下文工具类
 *
 * RequestContextHolder: Spring 提供的线程绑定工具，
 * 可以在任意地方获取当前请求的 HttpServletRequest（不需要在方法参数里传）
 * 原理：每个请求由独立线程处理，Spring 将请求信息绑定到当前线程
 */
public class RequestContextUtils {

    /**
     * 获取当前请求的 HttpServletRequest
     * 适用于在非 Controller 层（如 Service、AOP）获取请求信息
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取客户端IP地址
     * 支持通过代理转发后的 X-Forwarded-For 请求头获取真实IP
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) return "unknown";

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，第一个为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
