CREATE OR REPLACE TABLE `t_challenge_file`
(
    `id`            bigint(20)   NOT NULL COMMENT '主键',
    `challenge_id`  bigint(20)   NOT NULL COMMENT '题目id',
    `file_name`     varchar(255) NOT NULL DEFAULT '' COMMENT '文件名',
    `original_name` varchar(255) NOT NULL DEFAULT '' COMMENT '原名',
    `file_suffix`   varchar(10)  NOT NULL DEFAULT '' COMMENT '文件后缀名',
    `url`           varchar(500) NOT NULL COMMENT 'URL地址',
    `ext1`          text                  DEFAULT NULL COMMENT '扩展字段',
    `create_dept`   bigint(20)            DEFAULT NULL COMMENT '创建部门',
    `create_time`   datetime              DEFAULT NULL COMMENT '创建时间',
    `create_by`     bigint(20)            DEFAULT NULL COMMENT '上传人',
    `update_time`   datetime              DEFAULT NULL COMMENT '更新时间',
    `update_by`     bigint(20)            DEFAULT NULL COMMENT '更新人',
    `service`       varchar(20)  NOT NULL DEFAULT 'minio' COMMENT '服务商',
    PRIMARY KEY (`id`),
    KEY `t_challenge_file_challenge_id_index` (`challenge_id`),
    KEY `t_challenge_file_create_by_index` (`create_by`),
    KEY `t_challenge_file_create_dept_index` (`create_dept`),
    CONSTRAINT `t_challenge_file_t_challenge_id_fk` FOREIGN KEY (`challenge_id`) REFERENCES `t_challenge` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='题目文件表'

