-- AI 智能创作平台 - 数据库初始化
CREATE DATABASE IF NOT EXISTS ai_creator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_creator;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(50),
    avatar      VARCHAR(500),
    role        VARCHAR(30)  NOT NULL DEFAULT 'USER',
    status      TINYINT      NOT NULL DEFAULT 0,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创作内容表
DROP TABLE IF EXISTS ai_content;
CREATE TABLE ai_content (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    category_id   BIGINT       DEFAULT NULL,
    title         VARCHAR(200) DEFAULT NULL,
    content       LONGTEXT     DEFAULT NULL COMMENT '内容主体(Markdown)',
    summary       VARCHAR(500) DEFAULT NULL,
    template_type VARCHAR(30)  DEFAULT NULL COMMENT '模板类型',
    keywords      VARCHAR(500) DEFAULT NULL COMMENT '生成关键词',
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    is_favorite   TINYINT      DEFAULT 0,
    version       INT          NOT NULL DEFAULT 1,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_template (template_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作内容表';

-- 内容版本表
DROP TABLE IF EXISTS ai_content_version;
CREATE TABLE ai_content_version (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id  BIGINT       NOT NULL,
    version     INT          NOT NULL,
    title       VARCHAR(200) DEFAULT NULL,
    content     LONGTEXT     DEFAULT NULL,
    change_log  VARCHAR(500) DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_content (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容版本记录';

-- 分类表
DROP TABLE IF EXISTS ai_category;
CREATE TABLE ai_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200) DEFAULT NULL,
    sort_order  INT          DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容分类';

-- 默认数据
INSERT INTO sys_user (username, password, nickname, role) VALUES
('admin', '$2a$10$DPKhyadDfp2al.NK7lKo.OxhgwGlf3m4EIgfARBFQp0YNy5zKlWS.', '管理员', 'ADMIN'),
('user', '$2a$10$i9IDoRytIE2VnCniosO14uu4oO/nlSlz.sbcI6L4aDESzIOF6SnZK', '创作达人', 'USER');
