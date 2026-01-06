-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS aegis_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE aegis_auth;

-- 2. Spring Authorization Server 官方标准表 (必须存在且字段名一致)
CREATE TABLE oauth2_registered_client (
    id varchar(100) NOT NULL,
    client_id varchar(100) NOT NULL,
    client_id_issued_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret varchar(200) DEFAULT NULL,
    client_secret_expires_at timestamp DEFAULT NULL,
    client_name varchar(200) NOT NULL,
    client_authentication_methods varchar(1000) NOT NULL,
    authorization_grant_types varchar(1000) NOT NULL,
    redirect_uris varchar(1000) DEFAULT NULL,
    post_logout_redirect_uris varchar(1000) DEFAULT NULL,
    scopes varchar(1000) NOT NULL,
    client_settings varchar(2000) NOT NULL,
    token_settings varchar(2000) NOT NULL,
    PRIMARY KEY (id)
);

-- 3. 自定义用户表 (基础版)
CREATE TABLE sys_user (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    username varchar(64) NOT NULL COMMENT '用户名',
    password varchar(100) NOT NULL COMMENT '密码(BCrypt)',
    nickname varchar(64) DEFAULT NULL COMMENT '昵称',
    email varchar(100) DEFAULT NULL COMMENT '邮箱',
    status tinyint(4) DEFAULT '1' COMMENT '状态 1:正常 0:禁用',
    create_time datetime DEFAULT CURRENT_TIMESTAMP,
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 4. 初始化一个测试用户 (密码: 123456)
-- 注意：实际项目中密码必须是 BCrypt 加密后的字符串，这里仅作演示
INSERT INTO sys_user (username, password, nickname) 
VALUES ('admin', '$2a$10$7JB720yubVSZv5W8vNGkarOU7wG.8HayLB.9gJd.n.6F2tmdO', 'Admin User');