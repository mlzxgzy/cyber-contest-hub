create table t_competitor
(
    id          bigint auto_increment comment 'ID'
        primary key,
    name        varchar(90)                          not null comment '姓名',
    mobile      varchar(90)                          null comment '手机',
    idcard      varchar(255)                         null comment '身份证',
    create_by   varchar(255)                         null comment '创建人',
    create_time datetime default current_timestamp() not null comment '创建时间',
    update_by   varchar(255)                         null comment '更新人',
    update_time datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    remark      varchar(900)                         null comment '备注'
)
    comment '选手表';

