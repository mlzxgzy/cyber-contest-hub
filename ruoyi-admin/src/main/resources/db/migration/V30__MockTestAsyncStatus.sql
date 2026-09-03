-- 容器模拟测试支持异步启动：新增 starting/failed 状态与失败原因列
ALTER TABLE t_challenge_container_mock_test
    ADD COLUMN IF NOT EXISTS error_msg VARCHAR(500) DEFAULT NULL COMMENT '失败原因' AFTER status;

ALTER TABLE t_challenge_container_mock_test
    MODIFY COLUMN status VARCHAR(20) DEFAULT 'running' COMMENT '状态: starting/running/failed/destroying/expired';
