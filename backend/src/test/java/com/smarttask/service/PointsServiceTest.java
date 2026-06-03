package com.smarttask.service;

import com.smarttask.common.constant.CommonConstants;
import com.smarttask.entity.PointsLog;
import com.smarttask.entity.User;
import com.smarttask.mapper.PointsLogMapper;
import com.smarttask.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PointsService 单元测试
 *
 * 测试积分增加、签到、充值等功能。
 * 使用 Mockito 模拟 UserMapper、PointsLogMapper、RedissonClient。
 */
@ExtendWith(MockitoExtension.class)
class PointsServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PointsLogMapper pointsLogMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private PointsService pointsService;

    @Captor
    private ArgumentCaptor<PointsLog> pointsLogCaptor;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setPoints(100);
    }

    // ==================== 增加积分 ====================

    @Test
    @DisplayName("增加积分 - 成功增加并记录日志")
    void addPoints_success() {
        pointsService.addPoints(1L, 50, CommonConstants.POINTS_SIGN_IN, "签到获得50积分");

        // 验证更新用户积分
        verify(userMapper, times(1)).updatePoints(1L, 50);

        // 验证记录积分日志
        verify(pointsLogMapper, times(1)).insert(pointsLogCaptor.capture());
        PointsLog log = pointsLogCaptor.getValue();
        assertEquals(1L, log.getUserId());
        assertEquals(50, log.getPoints());
        assertEquals(CommonConstants.POINTS_SIGN_IN, log.getType());
    }

    @Test
    @DisplayName("扣除积分 - 负数积分表示扣减")
    void addPoints_negative_deduct() {
        pointsService.addPoints(1L, -30, "deduct", "购买商品扣除30积分");

        verify(userMapper).updatePoints(1L, -30);
        verify(pointsLogMapper).insert(pointsLogCaptor.capture());
        assertEquals(-30, pointsLogCaptor.getValue().getPoints());
    }

    // ==================== 签到 ====================

    @Test
    @DisplayName("签到 - 成功签到获得10积分")
    void signIn_success() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // hasSignedInToday → 查询今日签到记录 → 返回 0（未签到）
        when(pointsLogMapper.selectCount(any())).thenReturn(0L);

        pointsService.signIn(1L);

        // 验证增加了10积分并记录了日志
        verify(userMapper).updatePoints(1L, 10);
        verify(pointsLogMapper).insert(any(PointsLog.class)); // signIn → addPoints 记录一条日志
    }

    @Test
    @DisplayName("签到 - 已签到不再重复发放积分")
    void signIn_alreadySignedIn() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // hasSignedInToday → 查到已有记录 → 返回 1（已签到）
        when(pointsLogMapper.selectCount(any())).thenReturn(1L);

        pointsService.signIn(1L);

        // 验证没有增加积分
        verify(userMapper, never()).updatePoints(anyLong(), anyInt());
    }

    @Test
    @DisplayName("签到 - 获取锁失败时跳过签到")
    void signIn_lockFailed() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        pointsService.signIn(1L);

        // 验证没有执行任何积分操作
        verify(userMapper, never()).updatePoints(anyLong(), anyInt());
        verify(pointsLogMapper, never()).insert(any());
    }

    // ==================== 充值 ====================

    @Test
    @DisplayName("充值 - 充值 100 元获得 1000 积分")
    void recharge_success() {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        doNothing().when(lock).lock(anyLong(), any(TimeUnit.class));
        doNothing().when(lock).unlock();

        pointsService.recharge(1L, 100);

        verify(userMapper).updatePoints(1L, 1000); // 100 * 10 = 1000
        verify(pointsLogMapper).insert(pointsLogCaptor.capture());
        assertEquals(1000, pointsLogCaptor.getValue().getPoints());
        assertTrue(pointsLogCaptor.getValue().getDescription().contains("1000 积分"));
    }

    // ==================== 查询积分 ====================

    @Test
    @DisplayName("查询积分 - 用户存在时返回积分")
    void getUserPoints_userExists() {
        when(userMapper.selectById(1L)).thenReturn(sampleUser);

        Integer points = pointsService.getUserPoints(1L);

        assertEquals(100, points);
    }

    @Test
    @DisplayName("查询积分 - 用户不存在时返回 0")
    void getUserPoints_userNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        Integer points = pointsService.getUserPoints(999L);

        assertEquals(0, points);
    }

    @Test
    @DisplayName("签到检查 - 今天未签到返回 false")
    void hasSignedInToday_false() {
        when(pointsLogMapper.selectCount(any())).thenReturn(0L);

        boolean result = pointsService.hasSignedInToday(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("签到检查 - 今天已签到返回 true")
    void hasSignedInToday_true() {
        when(pointsLogMapper.selectCount(any())).thenReturn(1L);

        boolean result = pointsService.hasSignedInToday(1L);

        assertTrue(result);
    }
}
