package com.smarttask.aspect;

import com.smarttask.common.utils.RequestContextUtils;
import com.smarttask.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 操作日志 AOP 切面
 *
 * @Aspect: Spring AOP 注解，标识这是一个切面类
 *   AOP（Aspect Oriented Programming）面向切面编程：
 *   在不修改原有代码的情况下，在方法执行前后插入额外的逻辑
 *   适用于日志记录、权限校验、性能监控等横切关注点
 *
 * @Pointcut: 定义切点，即"在哪些方法上生效"
 * @Before: 在目标方法执行前运行
 * @AfterReturning: 在目标方法成功返回后运行
 *
 * 这里我们切 controller 包下的所有方法，记录操作日志
 * 比如：谁在什么时间调用了什么接口，传了什么参数，返回了什么结果
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    /**
     * 切点：匹配 com.smarttask.controller 包下的所有方法
     * execution 表达式语法：
     *   *        ⇒ 返回值任意
     *   ..       ⇒ 包层级任意
     *   *(..)    ⇒ 方法名任意，参数任意
     */
    @Pointcut("execution(* com.smarttask.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 前置通知：在 Controller 方法执行前记录请求信息
     */
    @Before("controllerPointcut()")
    public void beforeController(JoinPoint joinPoint) {
        HttpServletRequest request = RequestContextUtils.getRequest();
        if (request == null) return;

        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = "anonymous";
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            username = userDetails.getUsername();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("[操作日志] ════════════════════════════════════════");
        log.info("[操作日志] 用户: {} | IP: {}", username, RequestContextUtils.getClientIp());
        log.info("[操作日志] 接口: {} {}",
                request.getMethod(),
                request.getRequestURI());
        log.info("[操作日志] 方法: {}.{}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName());
        log.info("[操作日志] 时间: {}",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 后置通知：在 Controller 方法成功返回后记录结果
     *
     * returning = "result": 将 Controller 方法的返回值绑定到 result 参数
     */
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void afterControllerReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.info("[操作日志] 完成: {}.{} | 返回: {}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                result != null ? result.toString().substring(0, Math.min(result.toString().length(), 200)) : "null");
        log.info("[操作日志] ════════════════════════════════════════");
    }
}
