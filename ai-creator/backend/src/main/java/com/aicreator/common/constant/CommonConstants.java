package com.aicreator.common.constant;

public interface CommonConstants {
    String REDIS_TOKEN_PREFIX = "token:";
    String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

    String ROLE_ADMIN = "ADMIN";
    String ROLE_USER = "USER";

    // RabbitMQ
    String EXCHANGE_DIRECT = "ai.creator.direct";
    String QUEUE_AUDIT = "ai.creator.audit";
    String ROUTING_AUDIT = "audit";

    // Redis key
    String REDIS_AI_RATE_LIMIT = "ai:rate:";
    String REDIS_TEMPLATE_CACHE = "template:cache";
}
