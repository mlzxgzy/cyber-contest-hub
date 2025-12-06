-- 菜单 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997200624527527938, '题目草稿', '1997135864852934658', '20', 'challengeDraft', 'cch/challengeDraft/index', 1,
        0,
        'C', '0', '0', 'cch:challengeDraft:list', '#', 103, 1, sysdate(), null, null, '题目草稿菜单');

-- 按钮 SQL
insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997200624527527939, '题目草稿查询', 1997200624527527938, '1', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeDraft:query', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997200624527527940, '题目草稿新增', 1997200624527527938, '2', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeDraft:add', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997200624527527941, '题目草稿修改', 1997200624527527938, '3', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeDraft:edit', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997200624527527942, '题目草稿删除', 1997200624527527938, '4', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeDraft:remove', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
                             visible,
                      status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
values (1997200624527527943, '题目草稿导出', 1997200624527527938, '5', '#', '', 1, 0, 'F', '0', '0',
        'cch:challengeDraft:export', '#', 103, 1, sysdate(), null, null, '');
