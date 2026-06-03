package com.pzhu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * <p>
 * 【这是什么？】
 * 这是一个"数据模型"，对应数据库中的 operate_log 表。
 * 每当用户执行增、删、改操作时，系统会自动记录一条日志到这张表中。
 * <p>
 * 【Lombok 注解说明】
 *
 * @Data ：自动生成所有属性的 getter、setter、toString、equals、hashCode 方法
 * @NoArgsConstructor ：自动生成"无参构造方法"
 * @AllArgsConstructor ：自动生成"全参构造方法"（所有属性作为参数）
 * <p>
 * 【字段说明】
 * - id：日志的唯一编号（自增主键）
 * - operateUser：操作人（谁做的操作）
 * - operateTime：操作时间（什么时候做的）
 * - className：操作的类名（比如 DeptController）
 * - methodName：操作的方法名（比如 addDept）
 * - methodParams：方法参数（调用时传入了什么参数）
 * - returnValue：方法返回值（操作结果）
 * - costTime：方法执行耗时（花了多少毫秒）
 * - description：操作描述（比如"新增部门"）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateLog {
    private Integer id;            // 日志ID（主键，自增）
    private String operateUser;    // 操作人
    private LocalDateTime operateTime; // 操作时间
    private String className;      // 操作所在的类名
    private String methodName;     // 操作的方法名
    private String methodParams;   // 方法参数（JSON 格式）
    private String returnValue;    // 方法返回值（JSON 格式）
    private Long costTime;         // 方法执行耗时（毫秒）
    private String description;    // 操作描述
}
