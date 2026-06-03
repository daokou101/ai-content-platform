package com.aicreator.config;

import com.aicreator.common.constant.CommonConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public DirectExchange directExchange() { return new DirectExchange(CommonConstants.EXCHANGE_DIRECT); }
    @Bean
    public Queue auditQueue() { return QueueBuilder.durable(CommonConstants.QUEUE_AUDIT).build(); }
    @Bean
    public Binding auditBinding(DirectExchange e, Queue q) {
        return BindingBuilder.bind(q).to(e).with(CommonConstants.ROUTING_AUDIT);
    }
}
