package com.pzhu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 部门实体类 —— 对应数据库中的 dept 表
 *
 * 【什么是实体类？】
 * 实体类（POJO）就是"数据的容器"，用来封装从数据库中查到的数据。
 * 一张数据库表对应一个实体类，表中的每一行对应一个实体对象。
 * 比如 dept 表有一行数据 {id:1, name:"研发部"}，就对应一个 Dept 对象。
 *
 * 【类中的属性与数据库字段的对应关系】
 * 数据库字段：id, name, create_time, update_time
 * Java 属性： id, name, createTime, updateTime
 * 注意：数据库的 create_time（下划线命名）对应 Java 的 createTime（驼峰命名）
 * 这个转换是由 application.yml 中配置的 map-underscore-to-camel-case: true 实现的。
 *
 * 【@Data 注解（Lombok）】
 * 自动生成：所有属性的 getter 方法、setter 方法、toString() 方法、
 *          equals() 方法、hashCode() 方法。
 * 如果你写 dept.getId()，就是 @Data 自动生成的。
 *
 * 【@NoArgsConstructor 注解（Lombok）】
 * 自动生成"无参构造方法"：new Dept()
 *
 * 【@AllArgsConstructor 注解（Lombok）】
 * 自动生成"全参构造方法"：new Dept(1, "研发部", createTime, updateTime)
 *
 * 【什么是 LocalDateTime？】
 * Java 8 新增的时间类型，用来表示"日期+时间"（比如 2024-01-15 10:30:00）。
 * 相比旧的 Date 类型，更安全、更好用。
 */
@Data           // 自动生成 getter/setter/toString/equals/hashCode
@NoArgsConstructor  // 自动生成无参构造方法
@AllArgsConstructor // 自动生成全参构造方法
public class Dept {
    private Integer id;               // 部门ID（主键，自增）
    private String name;              // 部门名称
    private LocalDateTime createTime; // 创建时间（数据插入时自动设置）
    private LocalDateTime updateTime; // 修改时间（每次更新时自动设置）
}
