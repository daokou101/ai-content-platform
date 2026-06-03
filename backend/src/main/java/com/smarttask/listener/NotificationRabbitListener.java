package com.smarttask.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttask.common.constant.CommonConstants;
import com.smarttask.entity.Notification;
import com.smarttask.interceptor.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知消息的 RabbitMQ 监听器
 *
 * @RabbitListener: Spring AMQP 注解，标记方法为消息监听器
 *   queues 参数指定监听的队列名
 *   当队列中有新消息时，被注解的方法自动被调用
 *
 * @RabbitHandler: 配合 @RabbitListener 使用，标记具体处理消息的方法
 *   同一个监听器可以绑定多个 @RabbitHandler，根据消息类型分发
 *
 * 工作流程：
 *   1. RabbitMQ 收到通知消息
 *   2. 消息被路由到 smart.task.notification 队列
 *   3. 此监听器从队列中取出消息
 *   4. 通过 WebSocket 推送给对应用户
 *
 * 为什么需要 RabbitMQ 中间层？
 *   如果直接 WebSocket 推送，服务重启或推送失败时通知会丢失
 *   RabbitMQ 作为缓冲，保证消息不丢失（消息持久化）
 *   而且可以解耦：发送通知的业务代码不需要关心消息怎么推送给用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = CommonConstants.QUEUE_NOTIFICATION)
public class NotificationRabbitListener {

    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    @RabbitHandler
    public void handleNotification(Notification notification) {
        log.info("RabbitMQ 接收到通知消息: userId={}, title={}", notification.getUserId(), notification.getTitle());

        try {
            // 将通知对象转为 JSON 字符串
            String jsonMessage = objectMapper.writeValueAsString(notification);
            // 通过 WebSocket 推送给用户
            boolean sent = webSocketHandler.sendToUser(notification.getUserId(), jsonMessage);
            if (sent) {
                log.debug("通知已通过 WebSocket 推送给用户 {}", notification.getUserId());
            } else {
                log.info("用户 {} 不在线，通知已存入数据库，上线后展示", notification.getUserId());
            }
        } catch (Exception e) {
            log.error("处理通知消息异常", e);
        }
    }
}
