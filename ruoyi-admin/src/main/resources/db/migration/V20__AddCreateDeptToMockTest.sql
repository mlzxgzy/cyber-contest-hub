-- 为 t_challenge_container_mock_test 表添加 create_dept 列
ALTER TABLE t_challenge_container_mock_test
    ADD COLUMN IF NOT EXISTS create_dept BIGINT DEFAULT NULL COMMENT '创建部门' AFTER expose_info;
