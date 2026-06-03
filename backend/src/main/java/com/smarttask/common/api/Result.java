package com.smarttask.common.api;

import lombok.Data;

/**
 * 统一API响应结果封装
 * 所有接口统一返回此对象，保证前端解析格式一致
 *
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> {
    private int code;       // 业务状态码
    private String message; // 提示信息
    private T data;         // 返回数据
    private long timestamp; // 时间戳，便于前端排查问题

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> failed(ResultCode code) {
        Result<T> result = new Result<>();
        result.code = code.getCode();
        result.message = code.getMessage();
        return result;
    }

    public static <T> Result<T> failed(ResultCode code, String message) {
        Result<T> result = new Result<>();
        result.code = code.getCode();
        result.message = message;
        return result;
    }

    public static <T> Result<T> failed(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> failed(String message) {
        Result<T> result = new Result<>();
        result.code = ResultCode.FAILED.getCode();
        result.message = message;
        return result;
    }
}
