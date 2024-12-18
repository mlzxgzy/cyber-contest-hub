-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('竞赛', '0', '10', 'competition', 'exam/competition/index', 1, 0, 'C', '0', '0', 'exam:competition:list', '#', 'admin', sysdate(), '', null, '竞赛菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('竞赛查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'exam:competition:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('竞赛新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'exam:competition:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('竞赛修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'exam:competition:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('竞赛删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'exam:competition:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('竞赛导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'exam:competition:export',       '#', 'admin', sysdate(), '', null, '');