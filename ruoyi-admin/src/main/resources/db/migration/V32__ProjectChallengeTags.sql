-- 项目题目关联表增加标签字段：以逗号分隔的字符串形式存储，null 表示未打标签
alter table t_project_challenge
    add column tags varchar(500) null comment '标签（逗号分隔）' after version_id;
