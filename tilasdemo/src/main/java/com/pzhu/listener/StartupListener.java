package com.pzhu.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 * <p>
 * 【什么是监听器？】
 * 监听器是"观察者模式"的一种实现，它可以监听某个事件的发生，然后在事件发生时执行一些代码。
 * 就像订阅了公众号：当公众号发布文章（事件发生）时，你会收到通知。
 * <p>
 * 【这个监听器做了什么？】
 * 它监听了"应用启动完成"事件（ApplicationReadyEvent），
 * 当 Spring Boot 应用完全启动后，自动执行一些初始化操作。
 * <p>
 * 【实际应用场景】
 * - 应用启动后打印访问地址
 * - 初始化一些缓存数据
 * - 检查数据库连接是否正常
 * - 启动时加载配置信息
 * - 启动一些后台定时任务
 * <p>
 * 【@Component 注解】
 * 标记为 Spring 组件，让 Spring 自动扫描并注册这个监听器。
 * <p>
 * 【@EventListener 注解】
 * 标记方法为"事件监听器"，指定监听哪种事件。
 */
@Slf4j // Lombok：自动生成 log 日志对象
@Component // 标记为 Spring 组件
public class StartupListener {

    /**
     * 监听应用启动完成事件
     * <p>
     * 当 Spring Boot 应用完全启动成功后，会自动执行这个方法。
     * ApplicationReadyEvent 表示"应用已就绪"，此时所有的 Bean 都已经创建完成。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("========================================");
        log.info("    Tlias 智能学习辅助系统 启动成功！");
        log.info("    后端接口地址：http://localhost:8080");
        log.info("    前端访问地址：http://localhost:5173");
        log.info("========================================");
    }
}
