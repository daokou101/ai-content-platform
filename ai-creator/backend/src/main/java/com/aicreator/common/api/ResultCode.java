package com.aicreator.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    USERNAME_OR_PASSWORD_ERROR(4001, "用户名或密码错误"),
    USERNAME_EXISTS(4002, "用户名已存在"),
    AI_SERVICE_ERROR(5001, "AI服务调用失败"),
    AI_RATE_LIMIT(5002, "AI请求过于频繁，请稍后重试");

    private final int code;
    private final String message;
}
