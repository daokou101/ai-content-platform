package com.pzhu.config;

import com.pzhu.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 【为什么要全局异常处理？】
 * 如果没有统一处理异常，当程序出错时会直接把错误堆栈信息返回给前端，
 * 前端看到一堆看不懂的英文错误，体验很差。
 * 而且错误信息可能暴露系统内部细节，有安全风险。
 * <p>
 * 有了这个类，所有 Controller 抛出的异常都会集中到这里处理，
 * 统一返回格式为 {code: 0, msg: "错误描述"} 的 JSON 数据。
 * <p>
 * 【@RestControllerAdvice 注解】
 * = @ControllerAdvice + @ResponseBody
 * - @ControllerAdvice：全局控制器增强，可以捕获所有 Controller 的异常
 * - @ResponseBody：返回值直接写入 HTTP 响应体（返回 JSON）
 * <p>
 * 【@ExceptionHandler 注解】
 * 标记在方法上，指定这个方法处理哪种类型的异常。
 */
@Slf4j // 日志
@RestControllerAdvice // 全局异常处理
public class GlobalExceptionHandler {

    /**
     * 处理所有 Exception 类型的异常（"兜底"处理器）
     * <p>
     * 当某个方法抛出的异常没有特定的处理方法时，会由这个方法处理。
     * 相当于一个「安全网」—— 确保任何异常都不会漏掉。
     * <p>
     * 【@ExceptionHandler(Exception.class)】
     * 表示这个方法处理 Exception 及其所有子类异常。
     *
     * @param e 捕获到的异常对象
     * @return 统一返回的 Result 对象
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        // 打印错误日志（方便后端排查问题）
        log.error("系统异常：", e);

        // 返回友好的错误信息给前端
        return Result.error("服务器繁忙，请稍后重试：" + e.getMessage());
    }

    /**
     * 处理参数校验异常
     * <p>
     * 当请求参数不合法时（比如传了非数字的字符串给 Integer 字段），
     * Spring MVC 会抛出 MethodArgumentTypeMismatchException 等异常。
     *
     * @param e 参数类型转换异常
     * @return 错误提示
     */
    @ExceptionHandler(org.springframework.beans.TypeMismatchException.class)
    public Result handleTypeMismatch(Exception e) {
        log.warn("参数类型不匹配：{}", e.getMessage());
        return Result.error("请求参数格式不正确");
    }

    /**
     * 处理 HTTP 请求方法不支持的异常
     * <p>
     * 比如前端用 GET 请求调用了一个只支持 POST 的接口。
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public Result handleMethodNotSupported(Exception e) {
        log.warn("请求方法不支持：{}", e.getMessage());
        return Result.error("请求方法不正确");
    }

    /**
     * 处理 404 错误
     * <p>
     * 当前端请求了一个不存在的接口时会触发。
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public Result handleNotFound(Exception e) {
        log.warn("资源未找到：{}", e.getMessage());
        return Result.error("请求的资源不存在");
    }
}
