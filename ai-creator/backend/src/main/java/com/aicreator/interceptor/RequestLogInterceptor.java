package com.aicreator.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器
 * 记录每次请求的路径、耗时
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long start = (Long) request.getAttribute(START_TIME);
        if (start != null) {
            long cost = System.currentTimeMillis() - start;
            log.info("[请求日志] {} {} | 状态={} | 耗时={}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), cost);
        }
    }
}
