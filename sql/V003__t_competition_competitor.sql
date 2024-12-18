create table t_competition_competitor
(
    id               bigint auto_increment comment 'ID'
        primary key,
    competition_id   bigint       not null comment '竞赛ID',
    competition_name varchar(255) not null comment '竞赛名称',
    competitor_id    bigint       not null comment '选手ID',
    competitor_name  varchar(90)  not null comment '选手姓名',
    constraint t_competition_competitor_t_competitor_id_fk
        foreign key (id) references t_competitor (id)
            on update cascade,
    constraint t_competition_competitor_t_question_id_fk
        foreign key (competition_id) references t_question (id)
            on update cascade
)
    comment '竞赛选手表';

