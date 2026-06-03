package com.aicreator.aspect;

import com.aicreator.common.api.Result;
import com.aicreator.common.api.ResultCode;
import com.aicreator.common.exception.BusinessException;
import com.aicreator.security.JwtUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * AI 接口调用频率限制切面
 * 基于 Redis + 滑动窗口，限制每个用户每分钟最多调用 AI 接口 10 次
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_CALLS = 10;
    private static final int WINDOW_SECONDS = 60;
    private static final String RATE_KEY_PREFIX = "ai:rate:user:";

    @Pointcut("execution(* com.aicreator.controller.AiController.generate*(..))")
    public void aiControllerPointcut() {}

    @Around("aiControllerPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 获取当前用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtUser user)) {
            return pjp.proceed();
        }

        String key = RATE_KEY_PREFIX + user.getUserId();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (count != null && count > MAX_CALLS) {
            log.warn("AI调用频率超限: userId={}, count={}", user.getUserId(), count);
            return Result.failed(ResultCode.AI_RATE_LIMIT);
        }

        return pjp.proceed();
    }
}
