package com.smarttask.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarttask.entity.Notification;
import com.smarttask.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务
 *
 * @Async: Spring 异步注解，方法会在独立的线程池中执行
 * 不会阻塞主线程（用户不需要等待通知发送完成）
 * 线程池配置在 ThreadPoolConfig 中定义
 *
 * 同时通过 RabbitMQ 发送消息，由 WebSocket 推送给前端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final RabbitMQSender rabbitMQSender;

    /**
     * 发送通知（异步执行）
     * 1. 存入数据库
     * 2. 通过 RabbitMQ 发送消息，由消费者通过 WebSocket 推送给用户
     *
     * @Async: 这个方法会在独立线程中执行，调用方无需等待
     */
    @Async("taskExecutor")
    public void sendNotification(Long userId, String title, String content, String type, Long relatedId) {
        // 1. 通知存入数据库
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setRelatedId(relatedId);
        notificationMapper.insert(notification);

        // 2. 发送到 RabbitMQ，由监听器通过 WebSocket 推送给用户
        rabbitMQSender.sendNotification(notification);

        log.info("已发送通知给用户 {}: {}", userId, title);
    }

    /**
     * 获取用户未读通知数
     */
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    /**
     * 获取用户通知列表
     */
    public List<Notification> getUserNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectList(wrapper);
    }

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    /**
     * 全部标记为已读
     */
    public void markAllAsRead(Long userId) {
        List<Notification> unreadList = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );
        for (Notification n : unreadList) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }
}
