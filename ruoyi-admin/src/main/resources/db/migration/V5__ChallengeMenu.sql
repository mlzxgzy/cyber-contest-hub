-- 菜单 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997143724747923458, '题目列表', '1997135864852934658', '1', 'challenge', 'cch/challenge/index', 1, 0, 'C', '0',
        '0', 'cch:challenge:list', '#', 103, 1, sysdate(), null, null, '题目列表菜单');

-- 按钮 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997143724747923459, '题目列表查询', 1997143724747923458, '1', '#', '', 1, 0, 'F', '0', '0',
        'cch:challenge:query', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997143724747923460, '题目列表新增', 1997143724747923458, '2', '#', '', 1, 0, 'F', '0', '0',
        'cch:challenge:add', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997143724747923461, '题目列表修改', 1997143724747923458, '3', '#', '', 1, 0, 'F', '0', '0',
        'cch:challenge:edit', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997143724747923462, '题目列表删除', 1997143724747923458, '4', '#', '', 1, 0, 'F', '0', '0',
        'cch:challenge:remove', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (1997143724747923463, '题目列表导出', 1997143724747923458, '5', '#', '', 1, 0, 'F', '0', '0',
        'cch:challenge:export', '#', 103, 1, sysdate(), null, null, '');
