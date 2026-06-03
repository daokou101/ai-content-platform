package com.pzhu.dao;

import com.pzhu.pojo.Dept;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 部门管理数据访问层（DAO）
 *
 * 【什么是 DAO？】
 * DAO（Data Access Object）就是"数据访问对象"，
 * 负责与数据库进行交互，执行 SQL 语句。
 *
 * 【@Mapper 注解】
 * 这是 MyBatis 的注解，作用是告诉 Spring Boot：
 * "这个接口是 MyBatis 的映射器，需要在启动时为它创建代理对象"。
 * 有了 @Mapper，Spring 会自动实现这个接口，我们不需要写实现类。
 *
 * 【直接用 @Select/@Insert/@Delete/@Update 写 SQL】
 * 这种方式叫做"注解版 MyBatis"，直接在接口方法上写 SQL。
 * 优点：简单直观，适合简单的 SQL
 * 缺点：复杂 SQL 不好写
 * 对于复杂 SQL，使用 XML 文件（如 EmpDao.xml）
 *
 * 【#{} 占位符】
 * #{name} 表示从参数对象中取 name 属性的值，MyBatis 会自动替换为 ?（预编译）
 * 比如 Dept 对象有 name 属性，#{name} 就会被替换为 dept.getName() 的值
 */
@Mapper // 标记为 MyBatis 映射器接口
public interface DeptDao {

    /** 查询所有部门：SELECT * FROM dept */
    @Select("select * from dept")
    List<Dept> selectAll();

    /** 根据ID删除部门 */
    @Delete("delete from dept where id = #{id}")
    void deleteById(Integer id);

    /** 新增部门（插入数据） */
    @Insert("INSERT INTO dept(name, create_time, update_time) VALUES(#{name}, #{createTime}, #{updateTime})")
    void insert(Dept dept);

    /** 根据ID查询部门 */
    @Select("SELECT * FROM dept WHERE id = #{id}")
    Dept selectById(Integer id);

    /** 修改部门名称 */
    @Update("UPDATE dept SET name = #{name}, update_time = #{updateTime} WHERE id = #{id}")
    void updateById(Dept dept);
}
