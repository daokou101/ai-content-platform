package com.smarttask.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarttask.common.constant.CommonConstants;
import com.smarttask.entity.PointsLog;
import com.smarttask.mapper.PointsLogMapper;
import com.smarttask.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 积分管理服务
 *
 * 用户的积分体系是权限升级的核心：
 *   - 普通用户 → VIP用户：需要 500 积分
 *   - VIP用户 → 管理员：需要 2000 积分（由管理员审核）
 *   - 获取积分方式：签到、完成任务、充值
 *
 * 积分变动记录在 sys_points_log 表中，保证可追溯
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService {

    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;
    private final RedissonClient redissonClient;

    /** 签到锁的 Redis key 前缀 */
    private static final String SIGN_IN_LOCK_PREFIX = "lock:signin:";

    /**
     * 为用户增加/扣除积分
     *
     * @Transactional: 事务保证积分变动和日志记录的一致性
     *   points 为正数表示增加，负数表示扣减
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, int points, String type, String description) {
        // 更新用户积分（通过 Mapper 的 UPDATE 语句原子操作）
        userMapper.updatePoints(userId, points);

        // 记录积分变动日志
        PointsLog pointsLogEntry = new PointsLog();
        pointsLogEntry.setUserId(userId);
        pointsLogEntry.setPoints(points);
        pointsLogEntry.setType(type);
        pointsLogEntry.setDescription(description);
        pointsLogMapper.insert(pointsLogEntry);

        log.info("用户 {} 积分变动: {}({})，描述: {}", userId, points, type, description);
    }

    /**
     * 每日签到
     *
     * 使用 Redisson 分布式锁防止同一天在同一用户上重复签到
     * 如果不用分布式锁，多实例部署时同一个用户几乎同时发了两个签到请求，
     * hasSignedInToday 都返回 false，结果签到两次。
     *
     * RLock: Redisson 的分布式可重入锁
     *   tryLock(waitTime, leaseTime, unit):
     *     waitTime  - 等待锁的时间（0表示拿不到立即返回）
     *     leaseTime - 锁自动释放时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void signIn(Long userId) {
        RLock lock = redissonClient.getLock(SIGN_IN_LOCK_PREFIX + userId);
        try {
            // 等待 0 秒获取锁，持有 10 秒后自动释放
            if (!lock.tryLock(0, 10, TimeUnit.SECONDS)) {
                log.warn("签到获取锁失败，用户: {}", userId);
                return;
            }
            // 双重检查：获取锁后再检查一次今天是否已签到
            if (hasSignedInToday(userId)) {
                log.warn("用户 {} 今天已签到（锁内检查）", userId);
                return;
            }
            addPoints(userId, 10, CommonConstants.POINTS_SIGN_IN, "每日签到获得10积分");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("签到分布式锁中断", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 模拟充值
     *
     * 也加一把分布式锁防止并发充值导致积分计算错误
     */
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, Integer amount) {
        RLock lock = redissonClient.getLock("lock:recharge:" + userId);
        try {
            lock.lock(5, TimeUnit.SECONDS);
            int points = amount * 10;
            addPoints(userId, points, CommonConstants.POINTS_RECHARGE, "充值 " + amount + " 元，获得 " + points + " 积分");
            log.info("用户 {} 充值 {} 元，获得 {} 积分", userId, amount, points);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取用户当前积分
     */
    public Integer getUserPoints(Long userId) {
        com.smarttask.entity.User user = userMapper.selectById(userId);
        return user != null ? user.getPoints() : 0;
    }

    /**
     * 查询用户积分变动历史
     */
    public java.util.List<PointsLog> getPointsLogs(Long userId) {
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreateTime);
        return pointsLogMapper.selectList(wrapper);
    }

    /**
     * 检查用户今天是否已签到
     */
    public boolean hasSignedInToday(Long userId) {
        // 查询今日是否有签到记录
        java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .eq(PointsLog::getType, CommonConstants.POINTS_SIGN_IN)
                .ge(PointsLog::getCreateTime, todayStart);
        return pointsLogMapper.selectCount(wrapper) > 0;
    }
}
