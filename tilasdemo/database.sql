-- ============================================
-- Tlias 智能学习辅助系统 - 数据库建表脚本
-- ============================================
-- 使用说明：
-- 在 MySQL 中执行此文件，创建所需的数据库表
-- 执行命令：source database.sql
-- 或在 MySQL 客户端中复制粘贴执行

-- ============================================
-- 1. 部门表（Dept）
-- ============================================
-- 存储部门的基本信息
CREATE TABLE IF NOT EXISTS dept (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '部门ID（主键，自增）',
    name        VARCHAR(10)     NOT NULL                 COMMENT '部门名称（唯一）',
    create_time DATETIME        DEFAULT NULL             COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL             COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================
-- 2. 员工表（Emp）
-- ============================================
-- 存储员工的基本信息
CREATE TABLE IF NOT EXISTS emp (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '员工ID（主键，自增）',
    username    VARCHAR(20)     NOT NULL                 COMMENT '用户名（登录用，唯一）',
    password    VARCHAR(50)     DEFAULT '123456'         COMMENT '密码（默认123456）',
    name        VARCHAR(10)     NOT NULL                 COMMENT '真实姓名',
    gender      TINYINT UNSIGNED NOT NULL                COMMENT '性别（1=男，2=女）',
    phone       CHAR(11)        NOT NULL                 COMMENT '手机号（唯一）',
    job         TINYINT UNSIGNED DEFAULT NULL            COMMENT '职位（1=班主任,2=讲师,3=学工主管,4=教研主管,5=咨询师,6=其他）',
    salary      INT UNSIGNED    DEFAULT NULL             COMMENT '薪资',
    image       VARCHAR(300)    DEFAULT NULL             COMMENT '头像图片URL',
    entry_date  DATE            DEFAULT NULL             COMMENT '入职日期',
    dept_id     INT UNSIGNED    DEFAULT NULL             COMMENT '所属部门ID',
    create_time DATETIME        DEFAULT NULL             COMMENT '创建时间',
    update_time DATETIME        DEFAULT NULL             COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ============================================
-- 3. 用户表（User）—— 登录用
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '用户ID（主键，自增）',
    username    VARCHAR(20)     DEFAULT NULL             COMMENT '用户名',
    password    VARCHAR(32)     DEFAULT NULL             COMMENT '密码（MD5加密）',
    name        VARCHAR(10)     DEFAULT NULL             COMMENT '真实姓名',
    age         TINYINT UNSIGNED DEFAULT NULL            COMMENT '年龄',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（登录认证）';

-- 插入测试用户（密码都是 123456）
INSERT INTO user (username, password, name, age) VALUES
    ('admin', '123456', '管理员', 30),
    ('xiaoqiao', '123456', '小乔', 18),
    ('diaochan', '123456', '貂蝉', 24),
    ('lvbu', '123456', '吕布', 28);

-- ============================================
-- 4. 操作日志表（Operate_Log）—— 新增
-- ============================================
-- 记录用户的所有增、删、改操作
CREATE TABLE IF NOT EXISTS operate_log (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '日志ID（主键，自增）',
    operate_user    VARCHAR(20)     DEFAULT NULL             COMMENT '操作人（用户名）',
    operate_time    DATETIME        DEFAULT NULL             COMMENT '操作时间',
    class_name      VARCHAR(100)    DEFAULT NULL             COMMENT '操作所在的类名',
    method_name     VARCHAR(100)    DEFAULT NULL             COMMENT '操作的方法名',
    method_params   TEXT            DEFAULT NULL             COMMENT '方法参数（JSON格式）',
    return_value    TEXT            DEFAULT NULL             COMMENT '方法返回值（JSON格式）',
    cost_time       BIGINT          DEFAULT NULL             COMMENT '执行耗时（毫秒）',
    description     VARCHAR(255)    DEFAULT NULL             COMMENT '操作描述',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
