-- 修改字典数据表的 list_class 字段，将 danger 改为 error
UPDATE `sys_dict_data` SET `list_class` = 'error' WHERE `list_class` = 'danger';

-- 字典适配多语言
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_user_sex.male', `dict_type` = 'sys_user_sex' WHERE `dict_code` = 1;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_user_sex.female', `dict_type` = 'sys_user_sex' WHERE `dict_code` = 2;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_user_sex.unknown', `dict_type` = 'sys_user_sex' WHERE `dict_code` = 3;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_show_hide.show', `dict_type` = 'sys_show_hide' WHERE `dict_code` = 4;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_show_hide.hide', `dict_type` = 'sys_show_hide' WHERE `dict_code` = 5;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_normal_disable.normal', `dict_type` = 'sys_normal_disable' WHERE `dict_code` = 6;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_normal_disable.disable', `dict_type` = 'sys_normal_disable' WHERE `dict_code` = 7;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_yes_no.yes', `dict_type` = 'sys_yes_no' WHERE `dict_code` = 12;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_yes_no.no', `dict_type` = 'sys_yes_no' WHERE `dict_code` = 13;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_type.notice', `dict_type` = 'sys_notice_type' WHERE `dict_code` = 14;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_type.announcement', `dict_type` = 'sys_notice_type' WHERE `dict_code` = 15;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_status.normal', `dict_type` = 'sys_notice_status' WHERE `dict_code` = 16;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_status.close', `dict_type` = 'sys_notice_status' WHERE `dict_code` = 17;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.insert', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 18;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.update', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 19;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.delete', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 20;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.grant', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 21;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.export', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 22;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.import', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 23;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.force', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 24;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.gencode', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 25;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.clean', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 26;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_common_status.success', `dict_type` = 'sys_common_status' WHERE `dict_code` = 27;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_common_status.fail', `dict_type` = 'sys_common_status' WHERE `dict_code` = 28;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.other', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 29;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.password', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 30;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.sms', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 31;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.email', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 32;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.miniapp', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 33;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.social', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 34;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.pc', `dict_type` = 'sys_device_type' WHERE `dict_code` = 35;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.android', `dict_type` = 'sys_device_type' WHERE `dict_code` = 36;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.ios', `dict_type` = 'sys_device_type' WHERE `dict_code` = 37;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.miniapp', `dict_type` = 'sys_device_type' WHERE `dict_code` = 38;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.revoked', `dict_type` = 'wf_business_status' WHERE `dict_code` = 39;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.draft', `dict_type` = 'wf_business_status' WHERE `dict_code` = 40;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.pending', `dict_type` = 'wf_business_status' WHERE `dict_code` = 41;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.completed', `dict_type` = 'wf_business_status' WHERE `dict_code` = 42;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.cancelled', `dict_type` = 'wf_business_status' WHERE `dict_code` = 43;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.returned', `dict_type` = 'wf_business_status' WHERE `dict_code` = 44;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.terminated', `dict_type` = 'wf_business_status' WHERE `dict_code` = 45;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_form_type.custom_form', `dict_type` = 'wf_form_type' WHERE `dict_code` = 46;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_form_type.dynamic_form', `dict_type` = 'wf_form_type' WHERE `dict_code` = 47;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.revoke', `dict_type` = 'wf_task_status' WHERE `dict_code` = 48;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.pass', `dict_type` = 'wf_task_status' WHERE `dict_code` = 49;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.pending_review', `dict_type` = 'wf_task_status' WHERE `dict_code` = 50;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.cancel', `dict_type` = 'wf_task_status' WHERE `dict_code` = 51;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.return', `dict_type` = 'wf_task_status' WHERE `dict_code` = 52;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.terminate', `dict_type` = 'wf_task_status' WHERE `dict_code` = 53;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.transfer', `dict_type` = 'wf_task_status' WHERE `dict_code` = 54;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.delegate', `dict_type` = 'wf_task_status' WHERE `dict_code` = 55;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.copy', `dict_type` = 'wf_task_status' WHERE `dict_code` = 56;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.add_sign', `dict_type` = 'wf_task_status' WHERE `dict_code` = 57;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.minus_sign', `dict_type` = 'wf_task_status' WHERE `dict_code` = 58;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.timeout', `dict_type` = 'wf_task_status' WHERE `dict_code` = 59;

-- 目录类型菜单
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'carbon:cloud-service-management', `menu_name` = 'route.system' WHERE `menu_id` = 1;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'stash:dashboard', `menu_name` = 'route.monitor' WHERE `menu_id` = 2;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'tabler:tools', `menu_name` = 'route.tool' WHERE `menu_id` = 3;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'material-symbols:kid-star-outline', `menu_name` = 'route.demo' WHERE `menu_id` = 5;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'tabler:building-cog', `menu_name` = 'menu.system_tenant' WHERE `menu_id` = 6;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'tabler:logs', `menu_name` = 'menu.system_log' WHERE `menu_id` = 108;

-- 页面类型
UPDATE `sys_menu` SET `icon` = 'ic:round-manage-accounts', `menu_name` = 'route.system_user' WHERE `menu_id` = 100;
UPDATE `sys_menu` SET `icon` = 'carbon:user-role', `menu_name` = 'route.system_role' WHERE `menu_id` = 101;
UPDATE `sys_menu` SET `icon` = 'material-symbols:route', `menu_name` = 'route.system_menu' WHERE `menu_id` = 102;
UPDATE `sys_menu` SET `icon` = 'mingcute:department-line', `menu_name` = 'route.system_dept' WHERE `menu_id` = 103;
UPDATE `sys_menu` SET `icon` = 'hugeicons:permanent-job', `menu_name` = 'route.system_post' WHERE `menu_id` = 104;
UPDATE `sys_menu` SET `icon` = 'qlementine-icons:dictionary-16', `menu_name` = 'route.system_dict' WHERE `menu_id` = 105;
UPDATE `sys_menu` SET `icon` = 'carbon:parameter', `menu_name` = 'route.system_config' WHERE `menu_id` = 106;
UPDATE `sys_menu` SET `icon` = 'solar:chat-line-outline', `menu_name` = 'route.system_notice' WHERE `menu_id` = 107;
UPDATE `sys_menu` SET `icon` = 'majesticons:status-online-line', `menu_name` = 'route.monitor_online' WHERE `menu_id` = 109;
UPDATE `sys_menu` SET `icon` = 'simple-icons:redis', `menu_name` = 'route.monitor_cache' WHERE `menu_id` = 113;
UPDATE `sys_menu` SET `icon` = 'material-symbols:code-blocks-outline', `menu_name` = 'route.tool_gen' WHERE `menu_id` = 115;
UPDATE `sys_menu` SET `icon` = 'material-symbols:attach-file', `menu_name` = 'route.system_oss' WHERE `menu_id` = 118;
UPDATE `sys_menu` SET `icon` = 'tabler:building-skyscraper', `menu_name` = 'route.system_tenant' WHERE `menu_id` = 121;
UPDATE `sys_menu` SET `icon` = 'lets-icons:package-box-alt', `menu_name` = 'route.system_tenant-package' WHERE `menu_id` = 122;
UPDATE `sys_menu` SET `icon` = 'tabler:device-imac-cog', `menu_name` = 'route.system_client' WHERE `menu_id` = 123;
UPDATE `sys_menu` SET `icon` = 'carbon:operations-record', `menu_name` = 'route.monitor_operlog' WHERE `menu_id` = 500;
UPDATE `sys_menu` SET `icon` = 'tabler:login-2', `menu_name` = 'route.monitor_logininfor' WHERE `menu_id` = 501;
UPDATE `sys_menu` SET `icon` = 'gg:debug', `menu_name` = 'route.demo_demo' WHERE `menu_id` = 1500;
UPDATE `sys_menu` SET `icon` = 'gg:debug', `menu_name` = 'route.demo_tree' WHERE `menu_id` = 1506;
UPDATE `sys_menu` SET `path` = 'oss/config', `component` = 'system/oss-config/index', `icon` = 'hugeicons:configuration-01', `menu_name` = 'route.system_oss-config' WHERE `menu_id` = 133;

-- IFrame 类型
UPDATE `sys_menu` SET `component` = 'FrameView', `query_param` = '{"url": "https://ruoyi.xlsea.cn/admin/"}', `is_frame` = 2, `icon` = 'bx:bxl-spring-boot', `menu_name` = 'menu.monitor_admin' WHERE `menu_id` = 117;
UPDATE `sys_menu` SET `component` = 'FrameView', `query_param` = '{"url": "https://preview.snailjob.opensnail.com/"}', `is_frame` = 2, `icon` = 'gridicons:scheduled', `menu_name` = 'menu.monitor_snail-job' WHERE `menu_id` = 120;
-- 外链类型
UPDATE `sys_menu` SET `path` = 'https://gitee.com/xlsea/ruoyi-plus-soybean', `component` = 'FrameView', `icon` = 'local-icon-gitee', `menu_name` = 'RuoYi-Plus-Soybean' WHERE `menu_id` = 4;

-- plus-ui 需要禁用的页面
UPDATE `sys_menu` SET `status` = '1' WHERE `menu_id` IN ( '116', '130', '131', '132', '11700', '11701' );
