create table t_competition
(
    id          bigint auto_increment comment 'ID'
        primary key,
    name        varchar(255)                         not null comment '名称',
    date        datetime                             not null comment '日期',
    focus       varchar(255)                         not null comment '方向',
    create_by   varchar(255)                         null comment '创建人',
    create_time datetime default current_timestamp() not null comment '创建时间',
    update_by   varchar(255)                         null comment '更新人',
    update_time datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    remark      varchar(900)                         null comment '备注'
)
    comment '竞赛表';

