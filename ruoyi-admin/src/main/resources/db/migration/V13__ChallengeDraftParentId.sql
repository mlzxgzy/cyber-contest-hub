-- 为题目草稿表添加parent_id字段，用于记录派生关系，形成树状结构
alter table t_challenge_draft
    add column parent_id bigint null comment '派生父草稿ID' after id;

-- 添加索引以便快速查询子草稿
create index idx_parent_id on t_challenge_draft (parent_id);
