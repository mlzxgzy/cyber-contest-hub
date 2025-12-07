-- 菜单 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997603046311747585, '题目文件', '1997135864852934658', '40', 'challengeFile', 'cch/challengeFile/index', 1, 0,
        'C', '0', '0', 'cch:challengeFile:list', '#', 103, 1, sysdate(), null, null, '题目文件菜单');

-- 按钮 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997603046311747586, '题目文件查询', 1997603046311747585, '1', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeFile:query', '#', 103, 1, sysdate(), null, null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997603046311747587, '题目文件新增', 1997603046311747585, '2', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeFile:add', '#', 103, 1, sysdate(), null, null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997603046311747588, '题目文件修改', 1997603046311747585, '3', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeFile:edit', '#', 103, 1, sysdate(), null, null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997603046311747589, '题目文件删除', 1997603046311747585, '4', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeFile:remove', '#', 103, 1, sysdate(), null, null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997603046311747590, '题目文件导出', 1997603046311747585, '5', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeFile:export', '#', 103, 1, sysdate(), null, null, '');
