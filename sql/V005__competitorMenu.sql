-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('选手', '0', '20', 'competitor', 'exam/competitor/index', 1, 0, 'C', '0', '0', 'exam:competitor:list', '#', 'admin', sysdate(), '', null, '选手菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('选手查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'exam:competitor:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('选手新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'exam:competitor:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('选手修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'exam:competitor:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('选手删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'exam:competitor:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('选手导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'exam:competitor:export',       '#', 'admin', sysdate(), '', null, '');