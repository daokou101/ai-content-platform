package com.smarttask;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 智能任务协作平台 - 启动类
 *
 * @SpringBootApplication: Spring Boot 核心注解，组合了 @Configuration、@EnableAutoConfiguration、@ComponentScan
 * @EnableAsync: 启用 Spring 的异步方法执行能力，配合 @Async 注解实现异步调用（如：发通知、记日志等耗时操作丢给线程池处理）
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.smarttask.mapper")
public class SmartTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTaskApplication.class, args);
    }
}
