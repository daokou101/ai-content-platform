package com.pzhu.pojo;

import lombok.Data;

/**
 * 统一响应结果类 —— 所有接口都使用这个格式返回数据
 *
 * 【为什么需要这个类？】
 * 如果没有统一格式，每个接口返回的数据结构都不一样，
 * 前端处理起来会非常麻烦。
 * 有了 Result，所有接口的返回格式都是统一的：
 * {code: 1, msg: "success", data: ...}
 *
 * 【静态方法说明】
 * static 方法可以直接通过"类名.方法名()"调用，不需要创建对象。
 * - Result.success(data)：操作成功，返回数据
 * - Result.success()：操作成功，不返回数据
 * - Result.error("错误信息")：操作失败，返回错误信息
 *
 * 【code 编码规则】
 * - 1：成功
 * - 0：失败
 *
 * 【前端如何使用？】
 * const result = await queryAllDeptApi();
 * if (result.code) { // 非0即true，相当于判断是否成功
 *   // 处理数据
 *   deptList.value = result.data;
 * }
 */
@Data // Lombok：自动生成 getter/setter/toString
public class Result {
    private Integer code; // 状态码（1=成功，0=失败）
    private String msg;   // 提示信息
    private Object data;  // 返回的数据

    /**
     * 操作成功（带返回数据）
     * @param object 要返回给前端的数据
     * @return Result 对象
     */
    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.msg = "success";
        return result;
    }

    /**
     * 操作成功（不返回数据）
     * @return Result 对象
     */
    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    /**
     * 操作失败
     * @param msg 错误描述
     * @return Result 对象
     */
    public static Result error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
