package com.pzhu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 员工查询参数封装类 —— 把多个查询参数封装成一个对象
 *
 * 【为什么需要这个类？】
 * 当方法参数太多时（比如 page, pageSize, name, gender, begin, end 共6个参数），
 * 代码会变得很长且不易维护。把相关的参数封装成一个对象，代码更简洁。
 *
 * 【@DateTimeFormat 注解】
 * 告诉 Spring：前端传过来的日期字符串（如 "2024-01-15"）要转换成 LocalDate 类型。
 * pattern 指定了字符串的格式：yyyy 表示年，MM 表示月，dd 表示日。
 */
@Data                         // 自动生成 getter/setter/toString
@NoArgsConstructor            // 无参构造
@AllArgsConstructor           // 全参构造
public class EmpQueryBen {
    private Integer page;       // 当前页码（默认第1页）
    private Integer pageSize;   // 每页显示条数（默认10条）
    private String name;        // 员工姓名（用于模糊查询）
    private Integer gender;     // 性别（用于条件筛选）

    @DateTimeFormat(pattern = "yyyy-MM-dd")  // 前端传的日期格式：年-月-日
    private LocalDate begin;    // 入职开始日期（用于范围查询）

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;      // 入职结束日期（用于范围查询）
}
