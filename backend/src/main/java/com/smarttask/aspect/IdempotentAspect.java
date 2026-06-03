package com.smarttask.aspect;

import com.smarttask.common.annotation.Idempotent;
import com.smarttask.common.api.Result;
import com.smarttask.common.utils.RequestContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
@SuppressWarnings("all")
/**
 * 幂等性注解的 AOP 切面
 *
 * @Around: 环绕通知，在方法执行前后都执行逻辑
 *   这里在方法执行前检查幂等性，通过后才执行原方法
 *
 * 流程：
 *   1. 从请求头取 idempotent-token
 *   2. 用 Redisson 分布式锁 + Redis 做幂等检查
 *   3. 第一次请求：存入 Redis，正常执行方法
 *   4. 重复请求：直接拒绝，返回"请勿重复提交"
 *
 * 为什么同时用到分布式锁？
 *   如果不用锁，两个请求同时到达，都查 Redis 发现 key 不存在，
 *   然后都执行了方法——幂等性就失效了。
 *   分布式锁保证只有一个请求能通过检查。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedissonClient redissonClient;

    /** 幂等锁的 Redis key 前缀 */
    private static final String IDEMPOTENT_LOCK_PREFIX = "idempotent:lock:";
    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:key:";

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 从请求头中获取幂等键
        HttpServletRequest request = RequestContextUtils.getRequest();
        if (request == null) {
            return joinPoint.proceed();
            //----------------------------------这里的joinpoint.proceed代表什么含义
        }

        String idempotentKey = request.getHeader("idempotent-token");
        //request是以键值对的形式存在，通过get方法能获取到对应的值，比如(gidempotent-token: abc123)
        if (idempotentKey == null || idempotentKey.isEmpty()) {
            // 没有传幂等键，放行（兼容非幂等场景）
            return joinPoint.proceed();
        }

        // 通过分布式锁保证幂等检查的原子性
        RLock lock = redissonClient.getLock(IDEMPOTENT_LOCK_PREFIX + idempotentKey);
        //创建一个锁，锁的名字为IDEMPOTENT_LOCK_PREFIX（固定前缀）+上面获得的request中的key
        boolean locked = lock.tryLock(0, 5, TimeUnit.SECONDS);
        //尝试拥有锁

        if (!locked) {
            // 获取锁失败说明另一个线程正在处理，直接返回重复请求
            log.warn("幂等锁获取失败，重复请求: {}", idempotentKey);
            return Result.failed(idempotent.message());
               }
        try {
            // 检查 Redis 中是否已有处理成功的记录
            boolean alreadyProcessed = Boolean.TRUE.equals(
                    redissonClient.getBucket(IDEMPOTENT_KEY_PREFIX + idempotentKey).isExists()
                    //getBucket：操作redis数据库
                    //redissonClinet：redis的bean，在Resissonconfig类中创建的对象
            );

            if (alreadyProcessed) {
                log.warn("幂等校验不通过，重复请求: {}", idempotentKey);
                return Result.failed(idempotent.message());
        }

            // 标记为已处理（设置过期时间）
            redissonClient.getBucket(IDEMPOTENT_KEY_PREFIX + idempotentKey)
                    .set(true, idempotent.expireTime(), TimeUnit.SECONDS);
            //set(值, 过期时间, 单位)：往 Redis 存一个键值对

            // 执行原方法
            return joinPoint.proceed();

        } finally {
            lock.unlock();
        }
    }
}
