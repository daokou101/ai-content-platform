package com.pzhu.config;

import com.pzhu.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * <p>
 * 【这是什么？】
 * 这个类用来配置 Spring MVC 的一些行为，比如：
 * 1. 注册拦截器（哪些请求需要拦截，哪些不需要）
 * 2. 配置静态资源映射（让上传的图片可以被访问）
 * <p>
 * 【@Configuration 注解】
 * 告诉 Spring：这是一个"配置类"，类似于以前的 XML 配置文件。
 * Spring 会在启动时读取这个类中的配置并应用到项目中。
 * <p>
 * 【实现 WebMvcConfigurer 接口】
 * 这个接口定义了很多 Spring MVC 的配置方法，我们可以重写需要的方法。
 */
@Configuration // 标记为配置类
public class WebConfig implements WebMvcConfigurer {

    @Autowired // 注入登录拦截器
    private LoginInterceptor loginInterceptor;

    /**
     * 注册拦截器
     * <p>
     * 【哪些请求需要拦截？】
     * 需要登录才能访问的接口都需要拦截，比如：
     * - /depts/** （部门管理）
     * - /emps/** （员工管理）
     * - /upload （文件上传）
     * <p>
     * 【哪些请求不需要拦截？】
     * - /login （登录接口，如果拦截了就没人能登录了）
     * - /images/** （静态资源，不需要登录也能查看图片）
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)     // 添加拦截器
                .addPathPatterns("/**")                // 拦截所有请求
                .excludePathPatterns(                  // 排除不需要拦截的路径
                        "/login",                      // 登录接口
                        "/images/**",                  // 静态图片资源（头像）
                        "/error"                       // Spring Boot 错误页面
                );
    }

    /**
     * 配置静态资源映射
     * <p>
     * 【为什么要配置这个？】
     * 用户上传的图片保存在本地磁盘（如 E:/uploads/），
     * 但用户通过浏览器访问 http://localhost:8080/images/xxx.jpg 时，
     * Spring MVC 默认是找不到磁盘上的文件的，需要告诉它去哪里找。
     * <p>
     * 【addResourceHandler 和 addResourceLocations】
     * - addResourceHandler("/images/**")：URL 中以 /images/ 开头的请求
     * - addResourceLocations("file:E:/uploads/")：去本地磁盘的 E:/uploads/ 目录找文件
     * <p>
     * 注意：路径最后的斜杠不能少
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /images/** 的请求映射到本地文件系统的上传目录
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:E:/uploads/");
    }
}
