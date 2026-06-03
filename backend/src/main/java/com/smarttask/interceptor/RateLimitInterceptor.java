package com.smarttask.interceptor;

import com.smarttask.common.api.Result;
import com.smarttask.common.api.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流拦截器
 *
 * HandlerInterceptor: Spring MVC 的拦截器接口
 * 在请求到达 Controller 之前（preHandle），渲染视图之后（postHandle），请求完成之后（afterCompletion）执行
 *
 * 功能：使用 Redis + 滑动窗口算法对接口进行限流
 * 防止恶意请求刷接口，保护后端服务
 *
 * 限流策略：
 *   同一个IP在 10 秒内最多请求 10 次
 *   Redis key 设计: rate:limit:{IP地址}
 *   value: 请求次数
 *   TTL: 10秒
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    // Redis工具：用来记录每个IP的请求次数
    private final StringRedisTemplate redisTemplate;
    // JSON工具：把错误信息转成JSON返回给前端
    private final ObjectMapper objectMapper;
    // ==================== 限流规则 ====================
    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_SECONDS = 10;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)
            throws Exception {
        String clientIp = request.getRemoteAddr();
        String redisKey = "rate:limit:" + clientIp;

        // 3. Redis计数+1：每请求一次，数字就加1（原子操作，线程安全）
        Long count = redisTemplate.opsForValue().increment(redisKey);
        // 4. 如果是第一次请求（count=1），给这个key设置10秒过期时间
        if (count != null && count == 1) {
            // 第一次请求，设置过期时间
            redisTemplate.expire(redisKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }


        // 5. 判断：如果10秒内请求次数 > 10次 → 拦截！
        if (count != null && count > MAX_REQUESTS) {
            // 超过限流阈值，返回 429（Too Many Requests）
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(Result.failed(ResultCode.FAILED, "请求太频繁，请稍后再试"))
            );
            return false;
        }

        return true;
    }
}
