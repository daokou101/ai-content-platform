package com.pzhu.dao;

import com.pzhu.pojo.OperateLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志的 DAO（Data Access Object）层
 * <p>
 * 【什么是 DAO？】
 * DAO 是负责"跟数据库打交道"的层。
 * 这里定义了对 operate_log 表的操作方法，由 MyBatis 自动实现 SQL 执行。
 * <p>
 * 【@Mapper 注解】
 * 告诉 Spring Boot：这是一个 MyBatis 的映射器接口，需要为其创建代理对象。
 * Spring 会在启动时扫描所有带 @Mapper 的接口，并自动实现它们。
 * <p>
 * 【@Insert 注解】
 * 直接在接口方法上写 SQL，省去了写 XML 映射文件的麻烦。
 * #{operateUser} 表示取 OperateLog 对象的 operateUser 属性值。
 */
@Mapper
public interface LogDao {

    /**
     * 插入一条操作日志
     *
     * @param log 操作日志对象
     */
    @Insert("INSERT INTO operate_log(operate_user, operate_time, class_name, method_name, " +
            "method_params, return_value, cost_time, description) " +
            "VALUES(#{operateUser}, #{operateTime}, #{className}, #{methodName}, " +
            "#{methodParams}, #{returnValue}, #{costTime}, #{description})")
    void insert(OperateLog log);
}
