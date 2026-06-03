package com.smarttask.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * 为什么需要自定义线程池？
 *   默认的 @Async 使用 SimpleAsyncTaskExecutor，每次执行都创建一个新线程，不推荐
 *   自定义线程池可以精确控制线程数量、队列大小、拒绝策略等
 *
 * ThreadPoolTaskExecutor: Spring 封装的线程池实现，底层是 java.util.concurrent.ThreadPoolExecutor
 * 核心参数说明：
 *   corePoolSize: 核心线程数（一直存活，除非 allowCoreThreadTimeOut）
 *   maxPoolSize: 最大线程数（核心线程满了，队列也满了，再创建新线程直到此上限）
 *   queueCapacity: 任务队列容量（核心线程都在忙时，新任务进队列等待）
 *   keepAliveSeconds: 空闲线程存活时间（超出核心线程数的线程，空闲多久后被回收）
 *   rejectHandler: 拒绝策略（线程池和队列都满了，新任务怎么办？）
 *
 * 这里用到了 3 个线程池（可以按业务拆分，避免不同类型的任务互相影响）：
 *   taskExecutor: 处理 @Async 异步任务（发通知、记日志等）
 *   notificationExecutor: 专门处理 WebSocket 推送
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    @Value("${thread-pool.core-pool-size:10}")
    private int corePoolSize;
    //核心线程数

    @Value("${thread-pool.max-pool-size:20}")
    private int maxPoolSize;
    //最大线程数

    @Value("${thread-pool.queue-capacity:100}")
    private int queueCapacity;
    //队列容量

    @Value("${thread-pool.keep-alive-seconds:300}")
    private int keepAliveSeconds;
    //存活时间

    /**
     * 通用异步任务线程池
     * 用于 @Async 注解的方法，如发通知、记日志等
     *
     * AbortPolicy: 拒绝策略之一，直接抛出 RejectedExecutionException
     * CallerRunsPolicy: 由调用线程直接执行（不会丢失任务，但会阻塞调用方）
     * DiscardPolicy: 丢弃新任务
     * DiscardOldestPolicy: 丢弃最旧的任务
     *
     * 这里用 CallerRunsPolicy，牺牲一点性能保证任务不丢失
     */
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //new ThreadPoolTaskExcutor()创建一个线程池对象

        executor.setCorePoolSize(corePoolSize);

        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("task-exec-");     // 线程名前缀，方便排查问题
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 通知推送线程池（独立线程池，避免通知量大时影响业务线程）
     */
    @Bean("notificationExecutor")
    public ThreadPoolTaskExecutor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("notify-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
