-- 容器后端类型字典
INSERT IGNORE INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1998000000000000001, '000000', '容器后端类型', 'cch_container_backend_type', 103, 1, NOW(), 1, NOW(), '容器后端类型：docker/kubernetes');

INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1998000000000000001, '000000', 1, 'Docker', 'docker', 'cch_container_backend_type', '', 'primary', 'N', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1998000000000000002, '000000', 2, 'Kubernetes', 'kubernetes', 'cch_container_backend_type', '', 'info', 'N', 103, 1, NOW(), 1, NOW(), '');

-- 容器配置菜单
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (2100000000000000001, '容器管理', 0, 20, 'container', 'cch/container-config/index', '', 1, 1, 'C', '0', '0', 'container:config:list', 'mdi:docker', 103, 1, NOW(), 1, NOW(), '');

-- 容器配置权限按钮
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (2100000000000000002, '容器配置查询', 2100000000000000001, 1, '', '', '', 1, 0, 'F', '0', '0', 'container:config:query', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (2100000000000000003, '容器配置新增', 2100000000000000001, 2, '', '', '', 1, 0, 'F', '0', '0', 'container:config:add', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (2100000000000000004, '容器配置修改', 2100000000000000001, 3, '', '', '', 1, 0, 'F', '0', '0', 'container:config:edit', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (2100000000000000005, '容器配置删除', 2100000000000000001, 4, '', '', '', 1, 0, 'F', '0', '0', 'container:config:remove', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (2100000000000000006, '容器配置导出', 2100000000000000001, 5, '', '', '', 1, 0, 'F', '0', '0', 'container:config:export', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (2100000000000000007, '容器配置测试', 2100000000000000001, 6, '', '', '', 1, 0, 'F', '0', '0', 'container:config:test', '#', 103, 1, NOW(), 1, NOW(), '');

