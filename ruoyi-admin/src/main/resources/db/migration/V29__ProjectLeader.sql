-- 项目表新增项目负责人字段（手填人名）
alter table t_project
    add column leader varchar(64) null comment '项目负责人（手填人名）' after remark;
