create table t_question
(
    id          bigint auto_increment comment 'ID'
        primary key,
    name        varchar(90)                          not null comment '名称',
    description varchar(900)                         null comment '描述',
    category    varchar(255)                         not null comment '类型',
    difficult   int      default 0                   not null comment '难度',
    flag        varchar(900)                         not null comment 'Flag',
    create_by   varchar(255)                         null comment '创建人',
    create_time datetime default current_timestamp() not null comment '创建时间',
    update_by   varchar(255)                         null comment '修改人',
    update_time datetime default current_timestamp() not null on update current_timestamp() comment '修改时间',
    remark      varchar(900)                         null comment '备注'
)
    comment '题目表';

