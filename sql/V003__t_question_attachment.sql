create table t_question_attachment
(
    id            bigint auto_increment comment 'ID'
        primary key,
    question_id   bigint                               not null comment '题目ID',
    question_name varchar(90)                          not null comment '题目名称',
    name          varchar(255)                         not null comment '文件名',
    description   varchar(900)                         null comment '文件备注',
    path          varchar(900)                         not null comment '文件下载地址',
    type          varchar(90)                          null comment '文件类型',
    create_by     varchar(255)                         null comment '创建人',
    create_time   datetime default current_timestamp() not null comment '创建时间',
    update_by     varchar(255)                         null comment '更新人',
    update_time   datetime default current_timestamp() not null on update current_timestamp() comment '更新时间',
    remark        varchar(900)                         null comment '备注',
    constraint t_question_attachment_t_question_id_fk
        foreign key (question_id) references t_question (id)
            on update cascade
)
    comment '题目附件表';

