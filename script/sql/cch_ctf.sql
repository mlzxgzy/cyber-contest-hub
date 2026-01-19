-- ----------------------------
-- CTF 题目表
-- 包含题目的基本信息和当前版本
-- ----------------------------
create table ctf_challenge (
    challenge_id      bigint(20)      not null                   comment '题目ID',
    dept_id           bigint(0)       null default null          COMMENT '部门id',
    user_id           bigint(0)       null default null          COMMENT '用户id',
    direction         varchar(100)    not null                   comment '题目方向',
    title             varchar(200)    not null                   comment '题目标题',
    introduction      varchar(1000)   default ''                 comment '题目介绍',
    current_version   varchar(50)     default ''                 comment '当前版本号(tag)',
    draft_version     varchar(50)     default null               comment '草稿版本号',
    update_time       datetime                                   comment '更新时间',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    del_flag          char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    primary key (challenge_id)
) engine=innodb comment = 'CTF题目表';


-- ----------------------------
-- CTF 题目版本历史表
-- 记录每个题目的所有版本（包括草稿和已发布的版本）
-- ----------------------------
create table ctf_challenge_version (
    version_id        bigint(20)      not null                   comment '版本ID',
    challenge_id      bigint(20)      not null                   comment '题目ID',
    tenant_id         varchar(20)     default '000000'           comment '租户编号',
    version_no        varchar(50)     not null                   comment '版本号',
    title             varchar(200)    not null                   comment '题目标题',
    direction         varchar(100)    not null                   comment '题目方向',
    introduction      varchar(1000)   default ''                 comment '题目介绍',
    content           text            default null               comment '题目内容（JSON格式，包含题目详细信息）',
    is_draft          tinyint(1)      default 0                  comment '是否为草稿版本（0否 1是）',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    update_time       datetime                                   comment '更新时间',
    del_flag          char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    primary key (version_id)
) engine=innodb comment = 'CTF题目版本历史表';


-- ----------------------------
-- CTF 题目标签表
-- 用于标记题目的版本，类似于Git中的tag
-- ----------------------------
create table ctf_challenge_tag (
    tag_id            bigint(20)      not null                   comment '标签ID',
    challenge_id      bigint(20)      not null                   comment '题目ID',
    tenant_id         varchar(20)     default '000000'           comment '租户编号',
    tag_name          varchar(50)     not null                   comment '标签名称（版本号）',
    version_id        bigint(20)      not null                   comment '对应版本ID',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    primary key (tag_id)
) engine=innodb comment = 'CTF题目标签表';


-- ----------------------------
-- CTF 题目分类表
-- 用于对题目进行分类管理
-- ----------------------------
create table ctf_challenge_category (
    category_id       bigint(20)      not null                   comment '分类ID',
    tenant_id         varchar(20)     default '000000'           comment '租户编号',
    category_name     varchar(100)    not null                   comment '分类名称',
    category_desc     varchar(500)    default ''                 comment '分类描述',
    parent_id         bigint(20)      default 0                  comment '父分类ID',
    order_num         int(4)          default 0                  comment '显示顺序',
    status            char(1)         default '0'                comment '分类状态（0正常 1停用）',
    create_dept       bigint(20)      default null               comment '创建部门',
    create_by         bigint(20)      default null               comment '创建者',
    create_time       datetime                                   comment '创建时间',
    update_by         bigint(20)      default null               comment '更新者',
    update_time       datetime                                   comment '更新时间',
    del_flag          char(1)         default '0'                comment '删除标志（0代表存在 1代表删除）',
    primary key (category_id)
) engine=innodb comment = 'CTF题目分类表';


-- ----------------------------
-- CTF 题目与分类关联表
-- 多对多关系
-- ----------------------------
create table ctf_challenge_category_ref (
    challenge_id      bigint(20)      not null                   comment '题目ID',
    category_id       bigint(20)      not null                   comment '分类ID',
    primary key (challenge_id, category_id)
) engine=innodb comment = 'CTF题目与分类关联表';
