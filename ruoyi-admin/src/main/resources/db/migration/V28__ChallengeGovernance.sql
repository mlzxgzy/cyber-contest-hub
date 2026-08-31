-- ----------------------------
-- V28: 题目数据治理
-- 1. t_challenge 增加业务编码与状态字段（主数据治理）
-- 2. t_challenge_version 增加联合索引（版本号唯一性由应用层校验：
--    因逻辑删除统一置 del_flag=1，无法使用数据库唯一索引，避免同号版本软删后冲突）
-- 3. t_challenge_container_image 增加逻辑删除字段（防止已发版题目悬空引用镜像）
-- 4. t_challenge_version_export_task 增加重试次数字段
-- ----------------------------

-- 1.1 题目编码（唯一业务编码，格式 CH + 主键ID）
alter table t_challenge
    add column code varchar(32) null comment '题目编码（唯一业务编码）' after id;

-- 1.2 题目状态（0-草稿中，1-已入库，2-已停用）
alter table t_challenge
    add column status int default 0 not null comment '题目状态（0-草稿中，1-已入库，2-已停用）' after latest_version_id;

-- 1.3 历史数据回填
update t_challenge set code = concat('CH', id) where code is null or code = '';
update t_challenge set status = 1 where latest_version_id is not null;

-- 1.4 编码唯一索引
alter table t_challenge
    add unique index uk_challenge_code (code);

-- 2. 版本号查询索引（唯一性由应用层校验，见 ChallengeVersionServiceImpl）
alter table t_challenge_version
    add index idx_challenge_id_version_tag (challenge_id, version_tag);

-- 3. 镜像表逻辑删除
alter table t_challenge_container_image
    add column del_flag int default 0 not null comment '删除标志（0代表存在 1代表删除）' after error_message;

-- 4. 导出任务重试次数
alter table t_challenge_version_export_task
    add column retry_count int default 0 not null comment '重试次数' after task_status;
