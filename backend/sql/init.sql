-- =============================================
-- AI 内容创作平台 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 编码: utf8mb4（支持 emoji 和特殊字符）
-- =============================================

CREATE DATABASE IF NOT EXISTS smart_task
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_task;

-- =============================================
-- 0. 用户表（sys_user）
--
-- 系统用户表，存储用户登录信息和基本资料。
-- 该表原有，保留原结构不变。
-- 默认管理员账号：admin / admin123
--
-- 字段说明：
--   id             - 用户ID（主键自增）
--   username       - 登录用户名（唯一）
--   password       - 密码（BCrypt 加密存储）
--   nickname       - 昵称
--   avatar         - 头像URL
--   role           - 角色（SUPER_ADMIN/ADMIN/VIP_USER/NORMAL_USER）
--   points         - 积分
--   status         - 状态（0正常 1禁用）
--   level          - 权限等级
-- =============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username       VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password       VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname       VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    email          VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar         VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    phone          VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role           VARCHAR(20)  DEFAULT 'NORMAL_USER' COMMENT '角色',
    points         INT          DEFAULT 0 COMMENT '积分',
    status         INT          DEFAULT 0 COMMENT '状态: 0正常 1禁用',
    level          INT          DEFAULT 0 COMMENT '权限等级: 0普通用户 1VIP 2管理员 3超级管理员',
    last_login_ip  VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP',
    last_login_time DATETIME    DEFAULT NULL COMMENT '最后登录时间',
    deleted        TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除: 0正常 1已删',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员账号（密码: admin123，BCrypt 加密）
INSERT INTO sys_user (username, password, nickname, role, points, status, level)
VALUES ('admin', '$2a$10$KsPU48DorxBvSpHaDeRxIOeBjnKbrePBxX1ai3meguKRU4ZrpQgj6',
        '管理员', 'SUPER_ADMIN', 9999, 0, 3);

-- =============================================
-- 1. AI 内容表（ai_content）
--
-- 存储用户通过 AI 生成的内容，是系统的核心业务表。
-- 每次用户在 AI 生成页面点击"保存"后，数据写入此表。
--
-- 跟其他表的关系：
--   created_by    → sys_user.id（创建者）
--   category_id   → ai_content_category.id（所属分类）
--   id（主键）     → ai_content_version.content_id（版本历史）
--
-- 字段说明：
--   id             - 主键自增
--   title          - 内容标题，用户在保存时自动从关键词提取
--   summary        - 摘要，AI 生成的简要概忈
--   content        - LONGTEXT 类型正文，支持大量文本
--   keywords       - 用户输入的关键词，逗号分隔
--   template_type  - 模板类型（article/marketing/social/seo/summary）
--   category_id    - 所属分类ID
--   status         - draft（草稿）/ published（已发布）
--   favorite       - 0（未收藏）/ 1（已收藏）
--   created_by     - 创建者用户ID
--   create_time    - 创建时间
--   update_time    - 更新时间
--   deleted        - 0（正常）/ 1（逻辑删除）
-- =============================================
DROP TABLE IF EXISTS ai_content;
CREATE TABLE ai_content (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内容ID',
    title         VARCHAR(255) DEFAULT NULL COMMENT '标题',
    summary       TEXT         DEFAULT NULL COMMENT '摘要',
    content       LONGTEXT     DEFAULT NULL COMMENT '正文内容',
    keywords      VARCHAR(500) DEFAULT NULL COMMENT '关键词（逗号分隔）',
    template_type VARCHAR(50)  DEFAULT NULL COMMENT '模板类型: article/marketing/social/seo/summary',
    category_id   BIGINT       DEFAULT NULL COMMENT '分类ID',
    status        VARCHAR(20)  DEFAULT 'draft' COMMENT '状态: draft/published',
    favorite      TINYINT(1)   DEFAULT 0 COMMENT '是否收藏: 0否 1是',
    created_by    BIGINT       DEFAULT NULL COMMENT '创建者用户ID',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除: 0正常 1已删',
    INDEX idx_creator (created_by),
    INDEX idx_template (template_type),
    INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI内容表';

-- =============================================
-- 2. 内容分类表（ai_content_category）
--
-- 用户自定义的内容分类，用于对 AI 生成的内容进行归类。
-- 在内容管理页面的"分类管理"弹窗中维护。
--
-- 跟其他表的关系：
--   id → ai_content.category_id（一条内容引用一个分类）
-- =============================================
DROP TABLE IF EXISTS ai_content_category;
CREATE TABLE ai_content_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(100) NOT NULL COMMENT '分类名称（如：技术文章、营销案例）',
    created_by  BIGINT       DEFAULT NULL COMMENT '创建者用户ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除: 0正常 1已删'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容分类表';

-- =============================================
-- 3. 内容版本历史表（ai_content_version）
--
-- 记录每次内容修改时的快照，支持版本回滚。
-- 机制：
--   首次保存 → version = 1
--   每次更新 → version + 1，保存当前快照
--   回滚操作 → 覆盖当前内容 + 记录新版本（作为回滚快照）
--
-- 跟其他表的关系：
--   content_id → ai_content.id（属于哪条内容）
-- =============================================
DROP TABLE IF EXISTS ai_content_version;
CREATE TABLE ai_content_version (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '版本记录ID',
    content_id  BIGINT   NOT NULL COMMENT '关联的内容ID',
    version     INT      NOT NULL COMMENT '版本号（从1递增）',
    title       VARCHAR(255) DEFAULT NULL COMMENT '该版本保存时的标题',
    summary     TEXT         DEFAULT NULL COMMENT '该版本保存时的摘要',
    content     LONGTEXT     DEFAULT NULL COMMENT '该版本保存时的正文',
    created_by  BIGINT       DEFAULT NULL COMMENT '创建者用户ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '版本创建时间',
    INDEX idx_content (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容版本历史表';

-- =============================================
-- 4. AI 模板表（ai_template）
--
-- 定义 AI 生成内容的"模板类型"。
-- 每种模板对应一段 system_prompt，决定了 AI 生成内容时使用的角色和风格。
--
-- 为什么存数据库而不是硬编码？
--   1. 新增模板只需 INSERT，不改代码
--   2. 前端动态加载 GET /api/ai/templates
--   3. 未来可在管理后台编辑提示词
-- =============================================
DROP TABLE IF EXISTS ai_template;
CREATE TABLE ai_template (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    type          VARCHAR(50)  NOT NULL UNIQUE COMMENT '模板类型标识（article/marketing/social/seo/summary）',
    name          VARCHAR(100) NOT NULL COMMENT '模板显示名称（如：文章写作）',
    description   TEXT         DEFAULT NULL COMMENT '模板描述',
    system_prompt TEXT         DEFAULT NULL COMMENT 'AI系统提示词（system message）',
    sort_order    INT          DEFAULT 0 COMMENT '排序号（小的靠前）',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模板表';

-- =============================================
-- 5. AI 模型表（ai_model）
--
-- 维护可用的 AI 大模型列表。
-- 前端"选择模型"下拉框的数据来源。
--
-- 扩展方式：想接入通义千问、智谱等，插记录 + 实现客户端即可
-- =============================================
DROP TABLE IF EXISTS ai_model;
CREATE TABLE ai_model (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模型ID',
    name        VARCHAR(100) NOT NULL COMMENT '模型显示名称（如：DeepSeek Chat）',
    model       VARCHAR(100) NOT NULL UNIQUE COMMENT '模型标识（传给API的参数值）',
    provider    VARCHAR(50)  DEFAULT 'deepseek' COMMENT '提供商（deepseek/aliyun/zhipu）',
    description TEXT         DEFAULT NULL COMMENT '模型描述',
    is_default  TINYINT(1)   DEFAULT 0 COMMENT '是否默认选中: 0否 1是',
    sort_order  INT          DEFAULT 0 COMMENT '排序号（小的靠前）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

-- =============================================
-- 6. 插入种子数据
-- =============================================

-- 模板种子数据（系统内置 5 种模板）
-- INSERT IGNORE 确保重复执行不会报错
INSERT IGNORE INTO ai_template (type, name, description, system_prompt, sort_order) VALUES
('article',   '文章写作',   '生成高质量的文章内容',
 '你是一位专业的文章写手。请根据用户提供的关键词和提示，生成一篇结构完整、语言流畅、内容充实的文章。要求条理清晰，段落分明，字数在800-1500字之间。',
 1),
('marketing', '营销文案',   '生成营销推广文案',
 '你是一位资深的营销文案专家。请根据关键词生成有说服力、吸引眼球的营销文案。要求突出卖点，语言有感染力，适合目标受众阅读。',
 2),
('social',    '社交媒体',   '生成社交媒体帖子',
 '你是一位社交媒体运营专家。请生成适合社交媒体发布的短文案。要求语言简洁有力，有话题性，适合在微博、小红书等平台发布。',
 3),
('seo',       'SEO 优化',   '生成 SEO 优化内容',
 '你是一位 SEO 内容专家。请生成针对搜索引擎优化的内容。要求合理布局关键词，标题吸引点击，内容对用户有实际价值。',
 4),
('summary',   '内容摘要',   '生成内容摘要',
 '请根据用户提供的内容，生成简洁准确、要点清晰的摘要。要求保留核心信息，长度控制在200字以内。',
 5);

-- 模型种子数据（当前仅接入 DeepSeek）
INSERT IGNORE INTO ai_model (name, model, provider, description, is_default, sort_order) VALUES
('DeepSeek Chat',    'deepseek-chat',    'deepseek', 'DeepSeek 最新对话模型，擅长各类文本生成任务',    1, 1),
('DeepSeek Reasoner','deepseek-reasoner','deepseek', 'DeepSeek 推理模型，擅长复杂推理和深度分析',    0, 2);
