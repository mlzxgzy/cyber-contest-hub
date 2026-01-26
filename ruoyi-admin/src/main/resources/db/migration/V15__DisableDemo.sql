UPDATE cch.sys_menu t
SET t.visible = '1',
    t.status  = '1'
WHERE t.menu_id in (5, 1500, 1501, 1502, 1503, 1504, 1505, 1506, 1507, 1508, 1509, 1510, 1511);
