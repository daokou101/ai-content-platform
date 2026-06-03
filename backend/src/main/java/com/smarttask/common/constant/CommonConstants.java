package com.smarttask.common.constant;

/**
 * 系统常量定义
 * 集中管理所有常量，避免魔法值散落在代码各处
 */
public interface CommonConstants {

    /** Token 在 Redis 中的存储前缀 */
    String REDIS_TOKEN_PREFIX = "token:";

    /** 在线用户列表的 Redis key */
    String REDIS_ONLINE_USERS = "online:users";

    /** 默认用户头像 */
    String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

    // ========== 角色编码 ==========
    String ROLE_SUPER_ADMIN = "SUPER_ADMIN";   // 超级管理员
    String ROLE_ADMIN = "ADMIN";                // 管理员
    String ROLE_VIP_USER = "VIP_USER";          // VIP用户
    String ROLE_NORMAL_USER = "NORMAL_USER";    // 普通用户

    // ========== 内容状态 ==========
    String CONTENT_STATUS_DRAFT = "draft";       // 草稿
    String CONTENT_STATUS_PUBLISHED = "published"; // 已发布

    // ========== 积分操作类型 ==========
    String POINTS_SIGN_IN = "SIGN_IN";           // 每日签到
    String POINTS_TASK_COMPLETE = "TASK_DONE";   // 完成任务
    String POINTS_RECHARGE = "RECHARGE";         // 积分充值
    String POINTS_UPGRADE = "UPGRADE";           // 权限升级
    String POINTS_ADMIN_ADJUST = "ADMIN_ADJUST"; // 管理员调整

    // ========== RabbitMQ 队列和交换机 ==========
    String EXCHANGE_DIRECT = "smart.task.direct";             // 直连交换机
    String QUEUE_NOTIFICATION = "smart.task.notification";    // 通知队列
    String ROUTING_NOTIFICATION = "notification";             // 通知路由键

}
