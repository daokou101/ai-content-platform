package com.smarttask.config;

import com.smarttask.common.constant.CommonConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 *
 * RabbitMQ 核心概念：
 *   交换机（Exchange）: 消息的第一站，根据路由规则将消息分发到队列
 *   队列（Queue）: 消息的存储和转发单元
 *   绑定（Binding）: 将交换机和队列关联起来，指定路由规则
 *   路由键（Routing Key）: 消息的标签，交换机根据它来分发消息
 *
 * 架构设计：
 *   直连交换机（Direct Exchange）：路由键精确匹配，用来发送通知消息
 *   ┌──────────┐     ┌──────────────┐     ┌─────────────────┐
 *   │ 消息发送者 │────▶│ Direct交换器  │────▶│ notification队列│────▶ 监听器 → WebSocket推送
 *   └──────────┘     └──────────────┘     └─────────────────┘
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 直连交换机（Direct Exchange）
     * 消息的路由键和队列的绑定键完全匹配时，消息才会被路由到该队列
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(CommonConstants.EXCHANGE_DIRECT);
    }

    /**
     * 通知队列
     * durable = true: 队列持久化，RabbitMQ 重启后队列依然存在
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(CommonConstants.QUEUE_NOTIFICATION).build();
    }

    /**
     * 将通知队列绑定到直连交换机
     * 绑定键 = ROUTING_NOTIFICATION（"notification"）
     * 只有路由键为 "notification" 的消息才会进入此队列
     */
    @Bean
    public Binding notificationBinding(DirectExchange directExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue)
                .to(directExchange)
                .with(CommonConstants.ROUTING_NOTIFICATION);
    }
}
