-- 为CCH菜单下的子菜单添加icon
-- 题目列表: 使用清单/题库相关的icon
UPDATE sys_menu SET icon = 'material-symbols:quiz' WHERE menu_id = 1997143724747923458 AND icon = '#';

-- 题目草稿: 使用草稿/编辑相关的icon
UPDATE sys_menu SET icon = 'material-symbols:draft' WHERE menu_id = 1997200624527527938 AND icon = '#';

-- 题目版本: 使用版本/历史相关的icon
UPDATE sys_menu SET icon = 'material-symbols:history' WHERE menu_id = 1997204990919643137 AND icon = '#';

-- 题目文件: 使用文件/文件夹相关的icon
UPDATE sys_menu SET icon = 'material-symbols:folder-open' WHERE menu_id = 1997603046311747585 AND icon = '#';
