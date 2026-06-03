package com.smarttask.common.exception;

import com.smarttask.common.api.Result;
import com.smarttask.common.api.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @RestControllerAdvice: 是 @ControllerAdvice + @ResponseBody 的组合注解
 *   用于全局处理控制器层抛出的异常，将异常信息统一封装为 Result 返回
 *   比在每个 Controller 里写 try-catch 优雅得多
 *
 * 方法作用:
 *   handleBusinessException: 捕获自定义的业务异常
 *   handleValidationException: 捕获参数校验失败异常（如 @Valid 校验不通过）
 *   handleAccessDeniedException: 捕获 Spring Security 权限不足异常
 *   handleException: 兜底，捕获所有未处理的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public Result<Void> handleValidationException(Exception e) {
        log.warn("参数校验异常: ", e);
        return Result.failed(ResultCode.VALIDATE_FAILED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.failed(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.failed("服务器繁忙，请稍后重试");
    }
}
