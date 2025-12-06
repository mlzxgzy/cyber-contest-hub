-- 菜单 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997204990919643137, '题目版本', '1997135864852934658', '30', 'challengeVersion', 'cch/challengeVersion/index',
        1, 0, 'C', '0', '0', 'cch:challengeVersion:list', '#', 103, 1, sysdate(), null, null, '题目版本菜单');

-- 按钮 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997204990919643138, '题目版本查询', 1997204990919643137, '1', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeVersion:query', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997204990919643139, '题目版本新增', 1997204990919643137, '2', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeVersion:add', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997204990919643140, '题目版本修改', 1997204990919643137, '3', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeVersion:edit', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997204990919643141, '题目版本删除', 1997204990919643137, '4', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeVersion:remove', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997204990919643142, '题目版本导出', 1997204990919643137, '5', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeVersion:export', '#', 103, 1, sysdate(), null, null, '');
