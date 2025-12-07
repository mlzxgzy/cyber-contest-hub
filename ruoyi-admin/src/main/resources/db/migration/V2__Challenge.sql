create or replace table cch2.t_challenge
(
    id                bigint                               not null comment '主键'
        primary key,
    category          varchar(50)                          not null comment '题目类型',
    name              varchar(128)                         not null comment '题目名称',
    remark text default '' not null comment '题目备注',
    latest_version_id bigint                               null comment '题目最新版ID',
    create_dept       bigint                               null comment '创建部门',
    create_time       datetime default current_timestamp() not null comment '创建时间',
    create_by         bigint                               null comment '创建人',
    update_time       datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by         bigint                               null comment '更新人',
    del_flag          int      default 0                   null comment '删除标志'
)
    comment '题目表';

create or replace table cch2.t_challenge_draft
(
    id                    bigint                                                   not null comment '主键'
        primary key,
    challenge_id          bigint                                                   not null comment '题目ID',
    challenge_name        varchar(128)                 default ''                  not null comment '题目名称',
    challenge_description text                                                     null comment '草稿描述',
    config                longtext collate utf8mb4_bin default '{}'                not null comment '配置'
        check (json_valid(`config`)),
    create_dept           bigint                                                   null comment '创建部门',
    create_time           datetime                     default current_timestamp() not null comment '创建时间',
    create_by             bigint                                                   null comment '创建人',
    update_time           datetime                     default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by             bigint                                                   null comment '更新人',
    del_flag              int                          default 0                   null comment '删除标志',
    constraint t_challenge_draft_t_challenge_id_fk
        foreign key (challenge_id) references cch2.t_challenge (id)
)
    comment '题目草稿表';

create or replace table cch2.t_challenge_version
(
    id                  bigint                               not null comment '主键'
        primary key,
    challenge_id        bigint                               not null comment '题目ID',
    challenge_name      varchar(128)                         not null comment '题目名称',
    draft_id            bigint                               not null comment '草稿ID',
    version_tag         varchar(50)                          not null comment '版本号',
    version_description text                                 null comment '版本描述',
    create_dept         bigint                               null comment '创建部门',
    create_time         datetime default current_timestamp() not null comment '创建时间',
    create_by           bigint                               null comment '创建人',
    update_time         datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by           bigint                               null comment '更新人',
    del_flag            int      default 0                   null comment '删除标志',
    constraint t_challenge_version_ibfk_1
        foreign key (challenge_id) references cch2.t_challenge (id),
    constraint t_challenge_version_t_challenge_draft_id_fk
        foreign key (draft_id) references cch2.t_challenge_draft (id)
)
    comment '题目版本表';

create or replace index idx_create_by
    on cch2.t_challenge (create_by);

create or replace index idx_create_dept
    on cch2.t_challenge (create_dept);

create or replace index idx_name
    on cch2.t_challenge (name);

create or replace index t_challenge_category_index
    on cch2.t_challenge (category);

create or replace index t_challenge_del_flag_index
    on cch2.t_challenge (del_flag);

create or replace index t_challenge_latest_version_id_index
    on cch2.t_challenge (latest_version_id);

create or replace index idx_challenge_id
    on cch2.t_challenge_draft (challenge_id);

create or replace index t_challenge_draft_create_by_index
    on cch2.t_challenge_draft (create_by);

create or replace index t_challenge_draft_create_dept_index
    on cch2.t_challenge_draft (create_dept);

create or replace index t_challenge_draft_del_flag_index
    on cch2.t_challenge_draft (del_flag);

create or replace index challenge_id
    on cch2.t_challenge_version (challenge_id);

create or replace index t_challenge_version_create_by_index
    on cch2.t_challenge_version (create_by);

create or replace index t_challenge_version_create_dept_index
    on cch2.t_challenge_version (create_dept);

create or replace index t_challenge_version_del_flag_index
    on cch2.t_challenge_version (del_flag);

create or replace index t_challenge_version_version_tag_index
    on cch2.t_challenge_version (version_tag);

