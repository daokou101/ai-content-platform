package com.pzhu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类 —— 整个应用的入口
 *
 * 【SpringBootApplication 注解】
 * 这是一个"组合注解"，相当于以下三个注解的功能：
 * 1. @SpringBootConfiguration：标记这是一个 Spring Boot 配置类
 * 2. @EnableAutoConfiguration：让 Spring Boot 自动配置（会根据引入的依赖自动配置相关组件）
 * 3. @ComponentScan：自动扫描当前包及其子包中的所有组件（@Controller、@Service 等）
 *
 * 【main 方法】
 * 程序的入口点，调用 SpringApplication.run() 启动整个 Spring Boot 应用。
 * 启动后会：
 * 1. 启动内嵌的 Tomcat 服务器
 * 2. 加载配置文件（application.yml）
 * 3. 创建所有 Bean（Controller、Service、DAO 等）
 * 4. 等待接收 HTTP 请求
 */
@SpringBootApplication
public class TilasdemoApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(TilasdemoApplication.class, args);
    }

}
