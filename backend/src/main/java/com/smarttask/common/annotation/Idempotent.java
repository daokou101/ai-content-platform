package com.smarttask.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解
 *
 * 幂等性（Idempotent）：同一个请求无论执行多少次，结果都和第一次执行一样
 *
 * 场景：网络卡顿时用户点了两次"创建任务"或"提交订单"，
 * 没有幂等性就会重复创建两条数据，有了之后第二个请求直接返回"重复请求"
 *
 * 原理：
 *   1. 请求时带上一个唯一的幂等键（Idempotent Key / Token）
 *   2. 后端第一次处理时把这个键存到 Redis 并设置过期时间
 *   3. 同一个键再来第二次，直接从 Redis 里查到已存在，直接拒绝
 *
 * @Target(METHOD): 这个注解只能加在方法上
 * @Retention(RUNTIME): 运行期保留，AOP 切面在运行时通过反射读取
 *
 * 属性:
 *   value       - 幂等键的来源字段（默认从请求参数的 idempotentKey 取）
 *   expireTime  - 幂等键在 Redis 中的过期时间（过期后允许重复提交）
 *   message     - 重复请求时的提示信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /** 幂等键的 SpEL 表达式（默认从请求头的 idempotent-token 取） */
    String key() default "";

    /** 过期时间，单位秒（默认 10 秒内不允许重复提交） */
    int expireTime() default 10;

    /** 重复请求时的提示 */
    String message() default "请勿重复提交";
}
