package com.smarttask.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 *
 * MybatisPlusInterceptor: MyBatis-Plus 的插件容器
 * PaginationInnerInterceptor: 分页插件，自动在 SQL 尾部拼接 LIMIT 语句
 * 使用 selectPage() 时自动生效，无需手动写分页SQL
 */
/**
 * MyBatis-Plus 配置类
 * 专门配置 MP 的插件功能（分页、乐观锁、防全表更新等）
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 核心拦截器
     * 所有 MP 的插件都必须通过这个拦截器加载
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 1. 创建 MP 的拦截器对象
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 2. 添加【分页插件】，指定数据库类型为 MySQL
        // PaginationInnerInterceptor = 分页内部拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 3. 交给 Spring 容器管理
        return interceptor;
    }
}