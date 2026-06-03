package com.smarttask.common.exception;

import com.smarttask.common.api.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 当业务逻辑出现问题时抛出此异常，由全局异常处理器统一捕获并返回给前端
 * 比直接返回 Result.failed() 更优雅，可以在任何层级抛出，统一处理
 *
 * 属性: code - 业务状态码, message - 错误描述
 * 方法: 提供多种构造方式，可以直接传 ResultCode 或自定义 message
 * 返回: 无返回值，通过全局异常处理器捕获后封装为 Result 返回
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
