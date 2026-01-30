-- 容器模拟测试记录表
CREATE TABLE t_challenge_container_mock_test
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    draft_id       BIGINT   NOT NULL COMMENT '草稿ID（最终测试基于草稿配置）',
    source_type    VARCHAR(20) COMMENT '来源类型: draft/version',
    source_id      BIGINT COMMENT '来源ID（草稿ID或版本ID）',
    challenge_name VARCHAR(200) COMMENT '题目名称',
    container_ids  TEXT COMMENT '容器ID列表(JSON)',
    expose_info    TEXT COMMENT '暴露信息(JSON): [{name, host, protocol, internalPort, externalPort}]',
    status         VARCHAR(20) DEFAULT 'running' COMMENT '状态: running/destroying/expired',
    create_time    DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_time    DATETIME NOT NULL COMMENT '过期时间',
    extend_count   INT         DEFAULT 0 COMMENT '延长次数',
    del_flag       BIGINT      DEFAULT 0 COMMENT '删除标志',
    create_by      VARCHAR(64) COMMENT '创建者',
    update_by      VARCHAR(64) COMMENT '更新者',
    update_time    DATETIME COMMENT '更新时间'
) COMMENT '容器模拟测试记录表';

-- 索引
CREATE INDEX idx_container_mock_test_draft_id ON t_challenge_container_mock_test (draft_id);
CREATE INDEX idx_container_mock_test_status ON t_challenge_container_mock_test (status);
CREATE INDEX idx_container_mock_test_expire_time ON t_challenge_container_mock_test (expire_time);
