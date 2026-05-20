-- 菜单 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100001, '项目管理', '1997135864852934658', '20', 'project', 'cch/project/index', 1, 0, 'C',
        '0',
        '0', 'cch:project:list', 'material-symbols:folder-managed', 103, 1, sysdate(), null, null, '项目管理菜单');

-- 按钮 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100002, '项目查询', 2200000000000100001, '1', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:query', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100003, '项目新增', 2200000000000100001, '2', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:add', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100004, '项目修改', 2200000000000100001, '3', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:edit', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100005, '项目删除', 2200000000000100001, '4', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:remove', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100006, '添加成员', 2200000000000100001, '5', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:member:add', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100007, '移除成员', 2200000000000100001, '6', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:member:remove', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100008, '导入题目', 2200000000000100001, '7', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:challenge:import', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100009, '移除题目', 2200000000000100001, '8', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:challenge:remove', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100010, '上传竞赛文件', 2200000000000100001, '9', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:contest:file:upload', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time,
                             remark)
values (2200000000000100011, '删除竞赛文件', 2200000000000100001, '10', '#', '', 1, 0, 'F', '0', '0',
        'cch:project:contest:file:remove', '#', 103, 1, sysdate(), null, null, '');
