-- 容器镜像菜单
-- 容器镜像作为容器管理的子菜单

-- 容器镜像列表页面菜单
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100010, '容器镜像', 1997135864852934658, 50, 'challenge-container-image',
        'cch/challenge-container-image/index', '', 1, 1, 'C', '0', '0', 'cch:challengeContainerImage:list',
        'mdi:docker-container', 103, 1, NOW(), 1, NOW(), '');

-- 容器镜像权限按钮
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100011, '容器镜像查询', 2100000000000100010, 1, '', '', '', 1, 0, 'F', '0', '0',
        'cch:challengeContainerImage:query', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100012, '容器镜像新增', 2100000000000100010, 2, '', '', '', 1, 0, 'F', '0', '0',
        'cch:challengeContainerImage:add', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100013, '容器镜像修改', 2100000000000100010, 3, '', '', '', 1, 0, 'F', '0', '0',
        'cch:challengeContainerImage:edit', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100014, '容器镜像删除', 2100000000000100010, 4, '', '', '', 1, 0, 'F', '0', '0',
        'cch:challengeContainerImage:remove', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100015, '容器镜像导出', 2100000000000100010, 5, '', '', '', 1, 0, 'F', '0', '0',
        'cch:challengeContainerImage:export', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100016, '镜像上传', 2100000000000100010, 6, '', '', '', 1, 0, 'F', '0', '0',
        'cch:challengeContainerImage:upload', '#', 103, 1, NOW(), 1, NOW(), '');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache,
                             menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by,
                             update_time, remark)
VALUES (2100000000000100017, '镜像Load', 2100000000000100010, 7, '', '', '', 1, 0, 'F', '0', '0',
        'cch:challengeContainerImage:load', '#', 103, 1, NOW(), 1, NOW(), '');
