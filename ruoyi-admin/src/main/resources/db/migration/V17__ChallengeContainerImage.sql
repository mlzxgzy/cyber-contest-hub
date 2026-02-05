CREATE TABLE IF NOT EXISTS `t_challenge_container_image`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `challenge_id`  bigint(20)   NOT NULL COMMENT '题目ID',
    `image_name`    varchar(255) NOT NULL COMMENT '镜像名称（展示用，一般为 name:tag 形式）',
    `pull_address`  varchar(2048)          DEFAULT NULL COMMENT '镜像拉取地址（docker pull / 服务创建使用的完整地址，包含标签）',
    `image_size`    bigint(20)            DEFAULT NULL COMMENT '镜像大小(字节)',
    `file_path`     varchar(500) NOT NULL COMMENT '镜像文件存储路径',
    `file_hash`     varchar(64)           DEFAULT NULL COMMENT '镜像文件SHA256哈希值',
    `status`        varchar(20)  NOT NULL DEFAULT 'uploading' COMMENT '上传状态(uploading:上传中,uploaded:已上传,validating:验证中,available:可用,error:错误)',
    `progress`      decimal(5, 2)         DEFAULT 0.00 COMMENT '上传进度(百分比)',
    `error_message` text                  DEFAULT NULL COMMENT '错误信息',
    `create_dept`   bigint(20)            DEFAULT NULL COMMENT '创建部门',
    `create_time`   datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`     bigint(20)            DEFAULT NULL COMMENT '创建人',
    `update_time`   datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`     bigint(20)            DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_challenge_id` (`challenge_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_container_image_challenge` FOREIGN KEY (`challenge_id`) REFERENCES `t_challenge` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='挑战容器镜像表';
