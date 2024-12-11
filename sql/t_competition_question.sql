create table t_competition_question
(
    id               bigint auto_increment comment 'ID'
        primary key,
    competition_id   bigint       not null comment '竞赛ID',
    competition_name varchar(255) not null comment '竞赛名称',
    question_id      bigint       not null comment '题目ID',
    question_name    varchar(90)  not null comment '题目名称',
    constraint t_competition_question_t_competition_id_fk
        foreign key (competition_id) references t_competition (id)
            on update cascade,
    constraint t_competition_question_t_question_id_fk
        foreign key (question_id) references t_question (id)
            on update cascade
)
    comment '竞赛题目表';

