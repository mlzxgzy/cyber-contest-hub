-- 项目表新增出题meta信息字段（仅出题项目使用）
alter table t_project
    add column authoring_meta json null comment '出题meta信息（仅出题项目使用）' after leader;
