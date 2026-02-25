-- 项目成员邀请表
create table if not exists t_project_member_invite
(
    id              bigint                               not null comment '主键'
        primary key,
    project_id      bigint                               not null comment '项目ID',
    permission_type varchar(20)                          not null comment '权限类型（admin管理员, view_all仅查看所有题, view_own仅查看自己导入的题目）',
    invite_code     varchar(64)                          not null comment '邀请Code',
    expire_time     datetime                             not null comment '过期时间',
    create_dept     bigint                               null comment '创建部门',
    create_time     datetime default current_timestamp() not null comment '创建时间',
    create_by       bigint                               null comment '创建人',
    update_time     datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    update_by       bigint                               null comment '更新人',
    del_flag        int      default 0                   null comment '删除标志',
    constraint uk_project_member_invite_code
        unique (invite_code),
    constraint t_project_member_invite_t_project_id_fk
        foreign key (project_id) references t_project (id)
)
    comment '项目成员邀请表';

create index idx_project_id_invite on t_project_member_invite (project_id, del_flag);

