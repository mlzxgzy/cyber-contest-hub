-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目附件', '0', '13', 'questionAttachment', 'exam/questionAttachment/index', 1, 0, 'C', '0', '0', 'exam:questionAttachment:list', '#', 'admin', sysdate(), '', null, '题目附件菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目附件查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionAttachment:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目附件新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionAttachment:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目附件修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionAttachment:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目附件删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionAttachment:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('题目附件导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'exam:questionAttachment:export',       '#', 'admin', sysdate(), '', null, '');