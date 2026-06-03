package com.aicreator.aspect;

import com.aicreator.security.JwtUser;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AI 调用日志切面
 * 记录每次 AI 生成的请求关键词、耗时、用户
 */
@Slf4j
@Aspect
@Component
public class AiLogAspect {

    @Pointcut("execution(* com.aicreator.service.DeepSeekService.generate*(..))")
    public void aiServicePointcut() {}

    @Around("aiServicePointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        // 获取用户名
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtUser u) username = u.getUsername();

        // 获取关键词参数
        String keywords = "unknown";
        Object[] args = pjp.getArgs();
        if (args.length > 0 && args[0] instanceof String k) keywords = k;

        Object result = pjp.proceed();

        long cost = System.currentTimeMillis() - start;
        log.info("[AI调用] 用户={}, 关键词={}, 耗时={}ms", username, keywords, cost);

        return result;
    }
}
