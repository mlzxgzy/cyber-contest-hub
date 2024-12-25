-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目Tag', '0', '31', 'questionTag', 'exam/questionTag/index', 1, 0, 'C', '0', '0', 'exam:questionTag:list', '#', 'admin', sysdate(), '', null, '题目Tag菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目Tag查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionTag:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目Tag新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionTag:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目Tag修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionTag:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目Tag删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionTag:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目Tag导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionTag:export',       '#', 'admin', sysdate(), '', null, '');