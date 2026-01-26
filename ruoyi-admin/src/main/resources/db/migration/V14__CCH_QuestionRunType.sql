-- 题目运行类型字典
INSERT IGNORE INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time,
                                  update_by, update_time, remark)
VALUES (1997999712279167999, '000000', 'CCH题目运行类型', 'cch_question_run_type', 103, 1, '2025-12-06 10:43:15', 1,
        '2025-12-06 10:43:15', '');

-- 静态题目
INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class,
                                  list_class, is_default, create_dept, create_by, create_time, update_by, update_time,
                                  remark)
VALUES (1997999789685047399, '000000', 1, '静态题目', 'static', 'cch_question_run_type', '', 'default', 'N', 103, 1,
        '2025-12-06 10:43:33', 1, '2025-12-06 10:43:59', '');

-- 容器题目
INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class,
                                  list_class, is_default, create_dept, create_by, create_time, update_by, update_time,
                                  remark)
VALUES (1997999881393504399, '000000', 2, '容器题目', 'container', 'cch_question_run_type', '', 'default', 'N', 103, 1,
        '2025-12-06 10:43:55', 1, '2025-12-06 10:43:55', '');

-- 虚拟机题目
INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class,
                                  list_class, is_default, create_dept, create_by, create_time, update_by, update_time,
                                  remark)
VALUES (1997999962473594999, '000000', 3, '虚拟机题目', 'vm', 'cch_question_run_type', '', 'default', 'N', 103, 1,
        '2025-12-06 10:44:14', 1, '2025-12-06 10:44:14', '');

