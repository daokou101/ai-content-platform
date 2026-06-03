package com.smarttask.service;

import com.smarttask.common.constant.CommonConstants;
import com.smarttask.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ 消息发送者
 *
 * RabbitTemplate: Spring AMQP 提供的消息发送模板，封装了与 RabbitMQ 交互的细节
 * 类似 JdbcTemplate 对 JDBC 的封装，简化了消息的发送操作
 *
 * 流程：服务发送消息 → RabbitMQ 交换机 → 绑定队列 → 监听器消费
 * 这里我们只发送通知消息，由通知监听器接收并通过 WebSocket 推送给前端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送通知消息到 RabbitMQ
     *
     * convertAndSend: RabbitTemplate 的消息发送方法
     * 参数1: 交换机名称（Exchange）
     * 参数2: 路由键（Routing Key）
     * 参数3: 消息对象（会被序列化为 JSON）
     *
     * 交换机根据路由键将消息投递到对应的队列
     */
    public void sendNotification(Notification notification) {
        rabbitTemplate.convertAndSend(
                CommonConstants.EXCHANGE_DIRECT,
                CommonConstants.ROUTING_NOTIFICATION,
                notification
        );
        log.debug("已发送通知消息到RabbitMQ: notificationId={}", notification.getId());
    }
}
