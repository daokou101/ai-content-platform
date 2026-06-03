package com.pzhu.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类 —— 对应数据库中的 emp 表
 *
 * 【实体类 vs 数据库表】
 * 这个类的每个属性都对应 emp 表的一个字段。
 * 当 MyBatis 执行查询时，会自动把查询结果中的每一行数据封装成 Emp 对象。
 *
 * 【日期类型说明】
 * - LocalDate：只包含日期（年-月-日），如 entryDate（入职日期）
 * - LocalDateTime：包含日期和时间（年-月-日 时:分:秒），如 createTime（创建时间）
 *
 * 【deptName 字段】
 * 这个字段在 emp 表中并不存在，它是"扩展字段"。
 * 在查询员工时，通过 LEFT JOIN dept 联表查询，把部门名称也一起查出来，
 * 放在这个字段中方便前端显示。
 * 在 EmpDao.xml 中可以看到：SELECT emp.*, dept.name dept_name
 * 数据库字段 dept_name（下划线）→ Java 属性 deptName（驼峰命名）
 */
@Data // Lombok：自动生成 getter/setter/toString/equals/hashCode
public class Emp {
    private Integer id;           // 员工ID（主键，自增）
    private String username;      // 用户名（登录用）
    private String password;      // 密码
    private String name;          // 真实姓名
    private Integer gender;       // 性别（1=男，2=女）
    private String phone;         // 手机号
    private Integer job;          // 职位（1=班主任,2=讲师,3=学工主管,4=教研主管,5=咨询师,6=其他）
    private Integer salary;       // 薪资
    private String image;         // 头像图片URL
    private LocalDate entryDate;  // 入职日期
    private Integer deptId;       // 所属部门ID
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 修改时间

    private String deptName;      // 部门名称（联表查询出的额外字段，数据库中不存在）
}
