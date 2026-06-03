package com.smarttask.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回状态码枚举
 * 定义接口返回的业务状态码和提示信息
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有权限，请联系管理员"),
    NOT_FOUND(404, "资源不存在"),
    USERNAME_OR_PASSWORD_ERROR(4001, "用户名或密码错误"),
    ACCOUNT_DISABLED(4002, "账号已被禁用"),
    USERNAME_EXISTS(4003, "用户名已存在"),
    POINTS_INSUFFICIENT(4004, "积分不足"),
    CONTENT_NOT_FOUND(4005, "内容不存在"),
    CATEGORY_NOT_FOUND(4006, "分类不存在");

    private final int code;
    private final String message;
}
