-- 竞赛文件标签字典类型
INSERT IGNORE INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time,
                                  update_by, update_time, remark)
VALUES (1998000000000000101, '000000', '竞赛文件标签', 'cch_contest_file_tag', 103, 1, sysdate(), 1, sysdate(),
        '竞赛文件标签字典');

-- 竞赛文件标签字典数据
INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class,
                                  list_class, is_default, create_dept, create_by, create_time, update_by, update_time,
                                  remark)
VALUES (1998000000000000102, '000000', 1, '技术文件', '技术文件', 'cch_contest_file_tag', '', 'primary', 'N', 103, 1,
        sysdate(), 1, sysdate(), '');

INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class,
                                  list_class, is_default, create_dept, create_by, create_time, update_by, update_time,
                                  remark)
VALUES (1998000000000000103, '000000', 2, '赛事说明', '赛事说明', 'cch_contest_file_tag', '', 'success', 'N', 103, 1,
        sysdate(), 1, sysdate(), '');

INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class,
                                  list_class, is_default, create_dept, create_by, create_time, update_by, update_time,
                                  remark)
VALUES (1998000000000000104, '000000', 3, '其他', '其他', 'cch_contest_file_tag', '', 'info', 'N', 103, 1, sysdate(), 1,
        sysdate(), '');
