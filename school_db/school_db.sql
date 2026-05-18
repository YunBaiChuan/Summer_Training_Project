CREATE DATABASE spring_final DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use spring_final;

CREATE TABLE `user` (
  `id`          bigint PRIMARY KEY AUTO_INCREMENT,
  `nickname`    varchar(30)  NOT NULL COMMENT '昵称',
  `phone`       varchar(20)  NOT NULL UNIQUE,
  `password`    varchar(64)  NOT NULL,
  `avatar_url`  varchar(200) DEFAULT NULL,
  `credit`      int          DEFAULT 100,
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `category` (
  `id`   bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(30) NOT NULL UNIQUE
);
INSERT INTO category VALUES (1,'数码'),(2,'教材'),(3,'单车'),(4,'美妆'),(5,'衣物'),(6,'运动'),(7,'其它');

CREATE TABLE `product` (
  `id`          bigint PRIMARY KEY AUTO_INCREMENT,
  `seller_id`   bigint       NOT NULL COMMENT '卖家',
  `category_id` bigint       NOT NULL,
  `title`       varchar(100) NOT NULL,
  `price`       decimal(10,2) NOT NULL,
  `description` text,
  `status`      tinyint      DEFAULT 0 COMMENT '0在售 1已售 2下架',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    content VARCHAR(255) COMMENT '最后一条消息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
);

CREATE TABLE chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    sender_type VARCHAR(20) NOT NULL COMMENT 'user/assistant',
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
);

CREATE TABLE `order` (
  `id`           bigint PRIMARY KEY AUTO_INCREMENT,
  `product_id`   bigint      NOT NULL,
  `buyer_id`     bigint      NOT NULL,
  `seller_id`     bigint      NOT NULL,
  `final_price`  decimal(10,2) NOT NULL,
  `place`        varchar(100) DEFAULT NULL COMMENT '面交地点',
  `meet_time`    datetime     DEFAULT NULL,
  `status`       tinyint      DEFAULT 0 COMMENT '0待见面 1已完成 2已取消',
  `create_time`  datetime DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `feedback` (
  `id`           bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id`     bigint NOT NULL UNIQUE,
  `star`         tinyint NOT NULL CHECK (star between 1 and 5),
  `tag`          varchar(30) DEFAULT NULL COMMENT '描述相符/态度好/面交准时',
  `create_time`  datetime DEFAULT CURRENT_TIMESTAMP
);