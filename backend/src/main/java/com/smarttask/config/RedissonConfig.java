package com.smarttask.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁客户端配置
 *
 * Redisson: 基于 Redis 的 Java 驻内存数据网格（In-Memory Data Grid）
 * 提供了分布式锁（RLock）、信号量（RSemaphore）、原子累加器（RAtomicLong）等丰富的分布式数据结构
 *
 * 和直接用 RedisTemplate SETNX 的区别：
 *   - Redisson 的锁是"可重入"的，同一个线程可以多次获取同一把锁
 *   - 自动处理"锁超时"问题（看门狗 watchdog 机制，自动续期）
 *   - 自带"解锁"和"释放"的原子性保证，不会死锁
 *
 * 看门狗（Watchdog）：获取锁后默认每 10 秒检查一次，如果任务没完成就自动续期到 30 秒
 *   这就是 Redisson 比手写 SETNX 可靠的核心理由——手写 SETNX 很容易因为任务执行太久锁过期了还不知道
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectionPoolSize(8)
                .setConnectionMinimumIdleSize(2);
        return Redisson.create(config);
    }
}
