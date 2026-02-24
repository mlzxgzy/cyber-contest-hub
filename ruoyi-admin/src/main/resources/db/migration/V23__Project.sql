-- 项目主表
create table if not exists t_project
(
    id           bigint                               not null comment '主键'
        primary key,
    project_type varchar(20)                          not null comment '项目类型（normal普通项目, contest竞赛项目）',
    name         varchar(128)                         not null comment '项目名称',
    remark       text                                 null comment '备注',
    meta         longtext collate utf8mb4_bin         null comment 'JSON字段，存储竞赛项目的额外信息（竞赛时间段、相关文件等）'
        check (json_valid(`meta`) or `meta` is null),
    create_dept  bigint                               null comment '创建部门',
    create_time  datetime default current_timestamp() not null comment '创建时间',
    create_by    bigint                               null comment '创建人',
    update_time  datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by    bigint                               null comment '更新人',
    del_flag     int      default 0                   null comment '删除标志'
)
    comment '项目主表';

-- 项目成员关联表
create table if not exists t_project_member
(
    id              bigint                               not null comment '主键'
        primary key,
    project_id      bigint                               not null comment '项目ID',
    user_id         bigint                               not null comment '用户ID',
    permission_type varchar(20)                          not null comment '权限类型（admin管理员, view_all仅查看所有题, view_own仅查看自己导入的题目）',
    create_dept     bigint                               null comment '创建部门',
    create_time     datetime default current_timestamp() not null comment '创建时间',
    create_by       bigint                               null comment '创建人',
    update_time     datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by       bigint                               null comment '更新人',
    del_flag        int      default 0                   null comment '删除标志',
    constraint uk_project_member
        unique (project_id, user_id),
    constraint t_project_member_t_project_id_fk
        foreign key (project_id) references t_project (id),
    constraint t_project_member_sys_user_id_fk
        foreign key (user_id) references sys_user (user_id)
)
    comment '项目成员关联表';

-- 项目题目关联表
create table if not exists t_project_challenge
(
    id           bigint                               not null comment '主键'
        primary key,
    project_id   bigint                               not null comment '项目ID',
    challenge_id bigint                               not null comment '题目ID',
    version_id   bigint                               not null comment '题目版本ID',
    create_dept  bigint                               null comment '创建部门',
    create_time  datetime default current_timestamp() not null comment '创建时间',
    create_by    bigint                               null comment '创建人',
    update_time  datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by    bigint                               null comment '更新人',
    del_flag     int      default 0                   null comment '删除标志',
    constraint uk_project_challenge
        unique (project_id, challenge_id, version_id),
    constraint t_project_challenge_t_project_id_fk
        foreign key (project_id) references t_project (id),
    constraint t_project_challenge_t_challenge_id_fk
        foreign key (challenge_id) references t_challenge (id),
    constraint t_project_challenge_t_challenge_version_id_fk
        foreign key (version_id) references t_challenge_version (id)
)
    comment '项目题目关联表';

-- 竞赛文件表
create table if not exists t_contest_file
(
    id          bigint                               not null comment '主键'
        primary key,
    project_id  bigint                               not null comment '项目ID（竞赛项目）',
    oss_id      bigint                               not null comment 'OSS文件ID',
    file_tag    varchar(50)                          null comment '文件标签',
    create_dept bigint                               null comment '创建部门',
    create_time datetime default current_timestamp() not null comment '创建时间',
    create_by   bigint                               null comment '创建人',
    update_time datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by   bigint                               null comment '更新人',
    del_flag    int      default 0                   null comment '删除标志',
    constraint t_contest_file_t_project_id_fk
        foreign key (project_id) references t_project (id),
    constraint t_contest_file_sys_oss_id_fk
        foreign key (oss_id) references sys_oss (oss_id)
)
    comment '竞赛文件表';

-- 索引
create index idx_project_type on t_project (project_type, del_flag);
create index idx_create_by on t_project (create_by, del_flag);
create index idx_project_id_member on t_project_member (project_id, del_flag);
create index idx_user_id_member on t_project_member (user_id, del_flag);
create index idx_project_id_challenge on t_project_challenge (project_id, del_flag);
create index idx_challenge_id_challenge on t_project_challenge (challenge_id, del_flag);
create index idx_project_id_file on t_contest_file (project_id, del_flag);
create index idx_oss_id_file on t_contest_file (oss_id);
