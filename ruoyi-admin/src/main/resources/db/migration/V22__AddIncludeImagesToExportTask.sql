-- 为题目版本导出任务表添加 include_images 字段
ALTER TABLE t_challenge_version_export_task
    ADD COLUMN include_images TINYINT DEFAULT 0 COMMENT '是否导出容器镜像文件（0-否，1-是）' AFTER expire_time;
