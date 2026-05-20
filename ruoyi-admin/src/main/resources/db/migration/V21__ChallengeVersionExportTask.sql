-- 题目版本导出任务表
CREATE TABLE t_challenge_version_export_task
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    version_id    BIGINT NOT NULL COMMENT '题目版本ID（关联t_challenge_version）',
    version_tag   VARCHAR(100) COMMENT '版本号（冗余字段，便于查询）',
    task_status   TINYINT  DEFAULT 0 COMMENT '任务状态（0-待处理，1-处理中，2-已完成，3-失败）',
    oss_file_id   BIGINT COMMENT 'OSS文件ID（关联sys_oss表）',
    oss_file_name VARCHAR(500) COMMENT 'OSS文件名',
    file_size     BIGINT COMMENT '文件大小（字节）',
    download_url  VARCHAR(1000) COMMENT '临时下载链接（生成时填充）',
    expire_time   DATETIME COMMENT '文件过期时间（完成时间+保留时间）',
    error_message TEXT COMMENT '错误信息',
    create_by     VARCHAR(64) COMMENT '创建者',
    create_dept   BIGINT DEFAULT NULL COMMENT '创建部门',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by     VARCHAR(64) COMMENT '更新者',
    update_time   DATETIME COMMENT '更新时间',
    del_flag      BIGINT   DEFAULT 0 COMMENT '删除标志'
) COMMENT '题目版本导出任务表';

-- 索引
CREATE INDEX idx_export_task_version_id ON t_challenge_version_export_task (version_id);
CREATE INDEX idx_export_task_status ON t_challenge_version_export_task (task_status);
CREATE INDEX idx_export_task_expire_time ON t_challenge_version_export_task (expire_time);
CREATE INDEX idx_export_task_create_time ON t_challenge_version_export_task (create_time);

-- 说明：sys_config.config_id 为非自增且非空，需显式指定主键 ID
-- 当前项目中已占用的 ID 有：1, 2, 3, 5, 11，此处从 21 开始新增
INSERT IGNORE INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES (2024683895252860930, '题目版本导出-最大并发任务数', 'cch.export.maxConcurrent', '3', 'Y', 'admin', NOW(),
        '题目版本导出任务的最大并发执行数量，默认值为3');

-- 使用新的配置 ID，避免与已有配置冲突
INSERT IGNORE INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES (2024683895252860931, '题目版本导出-文件保留时间', 'cch.export.fileRetentionHours', '72', 'Y', 'admin', NOW(),
        '导出文件在OSS中的保留时间（单位：小时），默认值为72小时（3天）');
