package com.smarttask.common.utils;

import com.smarttask.common.constant.CommonConstants;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis Key 生成器
 * 统一管理 Redis 中存储的键名，避免散落在代码中
 */
public class RedisKeyBuilder {

    /** 构建 Token 的 Redis Key */
    public static String buildTokenKey(Long userId) {
        return CommonConstants.REDIS_TOKEN_PREFIX + userId;
    }

    /** 构建用户信息的 Redis Key */
    public static String buildUserKey(Long userId) {
        return "user:" + userId;
    }

    /** 构建在线用户 Key */
    public static String buildOnlineKey() {
        return CommonConstants.REDIS_ONLINE_USERS;
    }

    /** 构建任务缓存 Key */
    public static String buildTaskKey(Long taskId) {
        return "task:" + taskId;
    }

    /** 构建积分排名 Key */
    public static String buildPointsRankKey() {
        return "points:ranking";
    }
}
