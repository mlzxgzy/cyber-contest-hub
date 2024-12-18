create table t_question_tag
(
    id            bigint auto_increment comment 'ID'
        primary key,
    question_id   bigint       not null comment '题目ID',
    question_name varchar(90)  not null comment '题目名称',
    tag           varchar(255) not null comment 'Tag',
    constraint t_question_tag_t_question_id_fk
        foreign key (question_id) references t_question (id)
            on update cascade
)
    comment '题目Tag表';

