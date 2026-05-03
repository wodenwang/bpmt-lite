-- bpmt-lite minimal database
-- hbm schema source: /Users/wenzhewang/workspace/bpmt_project/riversoft/trunk/support/hbm2ddl/target/sql-bpmt-lite/mysql/create_model.sql
-- activiti schema source: /Volumes/vm/maven/repository/org/activiti/activiti-engine/5.16.3/activiti-engine-5.16.3.jar
-- quartz schema source: /Volumes/vm/maven/repository/com/riversoft/quartz-ddl/2.2.1/quartz-ddl-2.2.1.zip
-- data source: /Users/wenzhewang/workspace/bpmt_project/riversoft/package/database/bpmt_init_data.xlsx
SET NAMES utf8;
SET FOREIGN_KEY_CHECKS=0;

CREATE DATABASE IF NOT EXISTS `kyq` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `kyq`;

-- hbm2ddl platform schema
create table CM_BASE_CATELOG (ID varchar(100) not null comment '类别主键', BUSI_NAME varchar(200) not null comment '展示名', SORT integer not null comment '排序', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', DESCRIPTION varchar(500) comment '描述', primary key (ID)) comment='字典类别表' ENGINE=InnoDB;
create table CM_BASE_DATA (DATA_TYPE varchar(100) not null comment '数据分类', DATA_CODE varchar(200) not null comment '扩展字段主键', PARENT_CODE varchar(200) comment '父数据KEY', SHOW_NAME varchar(200) not null comment '翻译值', SORT integer not null comment '排序', EXTRA varchar(200) comment '扩展字段(用于自定义条件)', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', DESCRIPTION varchar(500) comment '描述', primary key (DATA_TYPE, DATA_CODE)) comment='基础数据' ENGINE=InnoDB;
create table CM_BASE_TYPE (DATA_TYPE varchar(100) not null comment '数据分类', CATELOG varchar(200) comment '目录(分类)', BUSI_NAME varchar(200) not null comment '展示名', SORT integer not null comment '排序', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', DESCRIPTION varchar(500) comment '描述', primary key (DATA_TYPE)) comment='基础数据类型' ENGINE=InnoDB;
create table CM_DOMAIN (DOMAIN_KEY varchar(100) not null comment '主键', SYS_FLAG integer default 0 not null comment '是否系统级别', BUSI_NAME varchar(200) not null comment '展示名', SORT integer not null comment '排序', ICON varchar(200) comment '展示名', COLUMNS varchar(200) not null comment '分列展示', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', DESCRIPTION varchar(500) comment '描述', PRI varchar(100) not null comment '权限资源', primary key (DOMAIN_KEY)) comment='域' ENGINE=InnoDB;
create table CM_HOME (ID varchar(100) not null comment 'ID', SYS_FLAG integer default 0 not null comment '是否系统级别', ACTION varchar(500) not null comment '目标地址', DOMAIN_KEY varchar(100) not null comment '所处域', NAME varchar(200) not null comment '展示名', COLUMN_INDEX integer not null comment '列位置', SORT integer not null comment '排序', HEIGHT integer comment '高度', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', PARAM_TYPE integer comment '控件动态参数', PARAM_SCRIPT longtext comment '控件动态参数', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='域首页' ENGINE=InnoDB;
create table CM_MENU (ID varchar(100) not null comment 'ID', SYS_FLAG integer default 0 not null comment '是否系统级别', PARENT_ID varchar(100) comment '父节点ID', ACTION varchar(500) comment '目标地址', DOMAIN_KEY varchar(100) not null comment '所处域', ICON varchar(200) comment '图标', NAME varchar(200) not null comment '展示名', OPEN_TYPE integer not null comment '打开类型', SORT integer not null comment '排序', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', PARAM_TYPE integer comment '控件动态参数', PARAM_SCRIPT longtext comment '控件动态参数', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='菜单' ENGINE=InnoDB;
create table CM_PRI (PRI_KEY varchar(100) not null comment '唯一主键', CATELOG_TYPE integer not null comment '模块类别(开发写死)', CATELOG_KEY varchar(100) not null comment '模块主键(开发写死)', BUSI_NAME varchar(100) not null comment '展示名(开发写死)', DESCRIPTION varchar(500) comment '描述(界面编辑)', TYPE integer not null comment '组合验证类型', CHECK_TYPE integer not null comment '验证脚本类型', CHECK_SCRIPT longtext not null comment '验证脚本', primary key (PRI_KEY)) comment='权限资源表' ENGINE=InnoDB;
create table CM_PRI_GROUP (GROUP_ID varchar(100) not null comment '组ID', PARENT_ID varchar(100) comment '父ID', LEAF_FLAG integer not null comment '是否叶子节点', SYS_FLAG integer not null comment '是否系统级别', NAME varchar(200) not null comment '展示名', SORT integer not null comment '排序', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', DESCRIPTION varchar(500) comment '描述', primary key (GROUP_ID)) comment='权限组' ENGINE=InnoDB;
create table CM_PRI_GROUP_RELATE (PRI_KEY varchar(100) not null comment '权限资源ID', GROUP_ID varchar(100) not null comment '权限组ID', CHECK_TYPE integer comment '验证脚本类型', CHECK_SCRIPT longtext comment '验证脚本', DESCRIPTION varchar(500) comment '描述', primary key (PRI_KEY, GROUP_ID)) comment='权限关联组' ENGINE=InnoDB;
create table DEV_FUNCTION (FUNCTION_KEY varchar(100) not null comment '函数名', CATELOG varchar(100) not null comment '函数类型', FUNCTION_TYPE integer not null comment '验证脚本类型', FUNCTION_SCRIPT longtext not null comment '验证脚本', DESCRIPTION varchar(500) comment '描述', EXAMPLE varchar(255) comment '示例', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', primary key (FUNCTION_KEY)) comment='自定义函数' ENGINE=InnoDB;
create table DEV_FUNCTION_CATELOG (CATE_KEY varchar(100) not null comment '分类KEY', PARENT_KEY varchar(100) comment '父KEY', BUSI_NAME varchar(200) not null comment '展示名', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', primary key (CATE_KEY)) comment='自定义函数-类别' ENGINE=InnoDB;
create table DEV_JOB (JOB_KEY varchar(100) not null comment '任务主键', ACTIVE_FLAG integer not null comment '是否启动', DESCRIPTION varchar(500) comment '描述', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', CREATE_UID varchar(100) not null comment '创建人', LOG_TABLE_NAME varchar(100) comment '日志表名', CRON_EXPRESSION varchar(100) not null comment 'CRON表达式', IS_TRANSACTION integer default 1 not null comment '是否起事务', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', primary key (JOB_KEY)) comment='调度框架配置' ENGINE=InnoDB;
create table DEV_QUEUE (QUEUE_KEY varchar(100) not null comment '队列主键', DESCRIPTION varchar(500) comment '描述', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', CREATE_UID varchar(100) not null comment '创建人', TABLE_NAME varchar(100) comment '队列表名', LOG_TABLE_NAME varchar(100) comment '日志表名', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', primary key (QUEUE_KEY)) comment='异步处理队列配置' ENGINE=InnoDB;
create table TB_COLUMN (TABLE_NAME varchar(100) not null comment '表名', NAME varchar(100) not null comment '列名', DESCRIPTION varchar(500) comment '描述', PRIMARY_KEY int not null comment '是否主键', AUTO_INCREMENT int not null comment '是否自增', REQUIRED int not null comment '是否必选', MAPPED_TYPE_CODE integer not null comment '类型', TOTAL_SIZE integer not null comment '长度', SCALE integer not null comment '数字精度', DEFAULT_VALUE varchar(200) comment '默认值', SORT integer not null comment '排序', MEMO varchar(500) comment '备注(管理员备注)', primary key (TABLE_NAME, NAME)) comment='动态表列配置' ENGINE=InnoDB;
create table TB_INDEX (TABLE_NAME varchar(100) not null comment '表名', INDEX_NAME varchar(100) not null comment '索引名', DESCRIPTION varchar(500) comment '描述', IS_UNIQUE int not null comment '是否唯一索引', primary key (TABLE_NAME, INDEX_NAME)) comment='动态表索引配置' ENGINE=InnoDB;
create table TB_INDEX_COLUMN (TABLE_NAME varchar(100) not null comment '表名', INDEX_NAME varchar(100) not null comment '索引名', COLUMN_NAME varchar(100) not null comment '列名', ORDINAL_POSITION integer not null comment '位置', INDEX_SIZE varchar(20) comment '长度', PRIMARY_KEY int not null comment '是否主键索引', primary key (TABLE_NAME, INDEX_NAME, COLUMN_NAME)) comment='动态表索引列配置' ENGINE=InnoDB;
create table TB_TABLE (NAME varchar(100) not null comment '表名', LOCK_FLAG integer default 0 not null comment '是否锁定(锁定则无法编辑)', CACHE_FLAG integer default 0 not null comment '是否使用缓存', DESCRIPTION varchar(500) comment '描述', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', CREATE_UID varchar(100) not null comment '创建人', primary key (NAME)) comment='动态表配置' ENGINE=InnoDB;
create table TPL_CURRENT (NAME varchar(100) not null comment '属性名', BUSI_NAME varchar(200) not null comment '展示名', PROPERTY_VALUE varchar(500) not null comment '属性值', primary key (NAME)) comment='当前模板' ENGINE=InnoDB;
create table TPL_SNAPSHOT (ID varchar(100) not null comment '流水(UUID)', SHOT_KEY varchar(200) not null comment '快照唯一健', NAME varchar(200) not null comment '快照名称', DESCRIPTION longtext not null comment '描述', VERSION integer not null comment '版本', PLATFORM_VERSION varchar(100) not null comment '平台版本', DB_FILE longblob not null comment '数据文件', MODIFIED_TABLES longtext comment '增量列表', COPYDATA_TABLES longtext comment '需拷贝数据动态表', CREATE_UID varchar(100) not null comment '创建人', CREATE_DATE datetime not null comment '创建时间', primary key (ID)) comment='模板快照' ENGINE=InnoDB;
create table TPL_SNAPSHOT_RECORD (ID bigint not null comment '自动主键', VERSION integer not null comment '版本', OPR_MEMO longtext not null comment '操作备注', OPR_CLASS varchar(500) comment '执行类', OPR_METHOD varchar(500) comment '执行方法', OPR_ARGS longtext comment '执行入参', CREATE_UID varchar(100) not null comment '创建人', CREATE_DATE datetime not null comment '创建时间', primary key (ID)) comment='开发操作记录' ENGINE=InnoDB;
create table US_GROUP (GROUP_KEY varchar(100) not null comment '组织主键', PARENT_KEY varchar(100) comment '上层组织', BUSI_NAME varchar(200) not null comment '展示名称', SYS_FLAG integer not null comment '是否系统级别', WX_DEPARTMENT_ID integer comment '微信部门ID', SORT integer default 0 not null comment '排序', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', primary key (GROUP_KEY)) comment='组织' ENGINE=InnoDB;
create table US_GROUP_ROLE (GROUP_KEY varchar(100) not null comment '组织主键主键', ROLE_KEY varchar(100) not null comment '角色主键', SYS_FLAG integer not null comment '是否系统级别', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', primary key (GROUP_KEY, ROLE_KEY)) comment='组织-角色关系表' ENGINE=InnoDB;
create table US_GROUP_TAG (GROUP_KEY varchar(100) not null comment '组织主键', TAG_KEY varchar(100) not null comment '标签主键', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', primary key (GROUP_KEY, TAG_KEY)) comment='组织-标签关系表' ENGINE=InnoDB;
create table US_ROLE (ROLE_KEY varchar(100) not null comment '角色主键', BUSI_NAME varchar(200) not null comment '展示名称', SYS_FLAG integer not null comment '是否系统级别', SORT integer default 0 not null comment '排序', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', primary key (ROLE_KEY)) comment='角色' ENGINE=InnoDB;
create table US_ROLE_GROUP_PRI_RELATE (ROLE_KEY varchar(100) not null comment '角色KEY', GROUP_KEY varchar(100) not null comment '组ID', GROUP_ID varchar(100) not null comment '权限组ID', primary key (ROLE_KEY, GROUP_KEY, GROUP_ID)) comment='角色组织权限组关联' ENGINE=InnoDB;
create table US_ROLE_PRI_GROUP_RELATE (ROLE_KEY varchar(100) not null comment '角色KEY', GROUP_ID varchar(100) not null comment '权限组ID', SYS_FLAG integer default 0 not null comment '是否系统级别', primary key (ROLE_KEY, GROUP_ID)) comment='角色权限组关联' ENGINE=InnoDB;
create table US_TAG (TAG_KEY varchar(100) not null comment '标签主键', BUSI_NAME varchar(200) not null comment '展示名称', WX_TAG_ID integer comment '微信标签ID', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', primary key (TAG_KEY)) comment='标签' ENGINE=InnoDB;
create table US_USER (USER_ID varchar(100) not null comment '用户ID', PASSWORD varchar(100) not null comment '用户密码', BUSI_NAME varchar(200) not null comment '展示名称', SYS_FLAG integer default 0 not null comment '是否系统级别', ACTIVE_FLAG integer not null comment '是否生效', SELECT_FLAG integer not null comment '是否控件中可选', SORT integer default 0 not null comment '排序', EFF_DATE datetime not null comment '生效时间', END_DATE datetime not null comment '失效时间', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', ALLOW_IP longtext comment '白名单IP', MOBILE varchar(100) comment '手机号', MAIL varchar(200) comment '邮箱', WXID varchar(200) comment '微信ID', WX_AVATAR longtext comment '微信头像URL', WX_ENABLE integer not null comment '微信开关', WX_STATUS integer comment '微信状态(关注状态:1=已关注;2=已冻结;4=未关注)', MSG_TYPE varchar(100) comment '接收消息类型(多选,分号隔离;MAIL:邮件;WX:微信企业号;)', RECEIVE_TYPE varchar(100) comment '接收消息范围(多选,分号隔离;USER:个人任务;GROUP:群组任务;)', primary key (USER_ID)) comment='操作员' ENGINE=InnoDB;
create table US_USER_GROUP_ROLE (USER_ID varchar(100) not null comment '用户ID', GROUP_KEY varchar(100) not null comment '组织主键主键', ROLE_KEY varchar(100) not null comment '角色主键', DEFAULT_FLAG integer not null comment '是否默认', SORT integer not null comment '排序', SYS_FLAG integer not null comment '是否系统级别', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', primary key (USER_ID, GROUP_KEY, ROLE_KEY)) comment='用户-组织-角色关系表' ENGINE=InnoDB;
create table US_USER_TAG (USER_ID varchar(100) not null comment '用户主键', TAG_KEY varchar(100) not null comment '标签主键', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', primary key (USER_ID, TAG_KEY)) comment='用户-标签关系表' ENGINE=InnoDB;
create table VW_DYN_BTN_ITEM (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', ACTION varchar(500) not null comment '目标地址', OPEN_TYPE integer default 1 not null comment '打开方式', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CONFIRM_MSG varchar(1000) comment '确认信息(提示框)', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='明细按钮(循环中每行展示)' ENGINE=InnoDB;
create table VW_DYN_BTN_SUMMARY (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE_CLASS varchar(100) not null comment '对齐方式(left/right)', ICON varchar(100) not null comment '图标', ACTION varchar(500) not null comment '目标地址', OPEN_TYPE integer default 1 not null comment '打开方式', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CONFIRM_MSG varchar(1000) comment '确认信息(提示框)', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='汇总按钮(表格底部展示)' ENGINE=InnoDB;
create table VW_DYN_BTN_SYS (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', TYPE integer not null comment '按钮类型(1:明细按钮/2:汇总按钮)', NAME varchar(100) not null comment '界面JS按钮名(编程装载事件,也用来与自定义按钮区分)', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) not null comment '对齐方式(left/right)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='系统内置按钮' ENGINE=InnoDB;
create table VW_DYN_COLUMN (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', NAME varchar(100) not null comment '字段名', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', SHOW_FLAG integer not null comment '是否展示', FORM_FLAG integer not null comment '是否表单', CONTENT_TYPE integer not null comment '内容脚本类型', CONTENT_SCRIPT longtext not null comment '内容脚本', STYLE varchar(500) comment '列样式', WHOLE integer not null comment '占据全行', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', WIDGET_CONTENT_TYPE integer comment '表单值(脚本类型)', WIDGET_CONTENT_SCRIPT longtext comment '表单值(脚本)', LIST_SORT integer not null comment '列表页排序', SORT integer not null comment '排序', EXEC_TYPE integer comment '前置处理脚本类型', EXEC_SCRIPT longtext comment '前置处理脚本', PRI varchar(100) not null comment '展示权限', CREATE_PRI varchar(100) comment '新增权限', UPDATE_PRI varchar(100) comment '修改权限', primary key (ID)) comment='动态表列视图模块' ENGINE=InnoDB;
create table VW_DYN_COLUMN_FORM (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', NAME varchar(200) not null comment '表单名', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '列样式', WHOLE integer not null comment '占据全行', CONTENT_TYPE integer not null comment '内容脚本类型', CONTENT_SCRIPT longtext not null comment '内容脚本', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', EDIT_PRI varchar(100) not null comment '编辑权限', primary key (ID)) comment='动态表表单列视图模块' ENGINE=InnoDB;
create table VW_DYN_COLUMN_LINE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '视图主键', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', EXPAND_FLAG integer not null comment '是否展开', SORT integer not null comment '排序', PRI varchar(100) not null comment '展示权限', primary key (ID)) comment='字段分割线' ENGINE=InnoDB;
create table VW_DYN_COLUMN_SHOW (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '列样式', WHOLE integer not null comment '占据全行', SORT_FIELD varchar(100) comment '排序字段', CONTENT_TYPE integer not null comment '内容脚本类型', CONTENT_SCRIPT longtext not null comment '内容脚本', SORT integer not null comment '排序', LIST_SORT integer not null comment '列表页排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='动态表展示列视图模块' ENGINE=InnoDB;
create table VW_DYN_EXEC_AFTER (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='后置处理器' ENGINE=InnoDB;
create table VW_DYN_EXEC_BEFORE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='前置处理器' ENGINE=InnoDB;
create table VW_DYN_EXEC_PREPARE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', VAR varchar(100) not null comment '变量(别名)', SORT integer not null comment '排序', primary key (ID)) comment='数据准备处理器' ENGINE=InnoDB;
create table VW_DYN_LIMIT (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', DESCRIPTION varchar(500) not null comment '描述', SQL_TYPE integer not null comment '内容脚本类型', SQL_SCRIPT longtext not null comment '内容脚本', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='动态表数据约束规则' ENGINE=InnoDB;
create table VW_DYN_PARENT (PARENT_KEY varchar(100) not null comment '主键KEY', VIEW_KEY varchar(100) not null comment '对应视图KEY', TABLE_NAME varchar(200) not null comment '父表名', SORT integer not null comment '排序', VAR varchar(100) not null comment '变量(别名)', DESCRIPTION varchar(500) comment '描述', primary key (PARENT_KEY)) comment='父表配置(左关联配置)' ENGINE=InnoDB;
create table VW_DYN_PARENT_FOREIGN (ID bigint not null comment '自动主键', PARENT_KEY varchar(100) not null comment '父KEY', MAIN_COLUMN varchar(100) not null comment '主表字段', PARENT_COLUMN varchar(100) not null comment '父表字段', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', primary key (ID)) comment='外键' ENGINE=InnoDB;
create table VW_DYN_QUERY (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', NAME varchar(100) not null comment '字段名', BUSI_NAME varchar(200) not null comment '展示名', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', DEF_VAL varchar(100) comment '默认值', SORT integer not null comment '排序', primary key (ID)) comment='动态表查询条件视图模块' ENGINE=InnoDB;
create table VW_DYN_QUERY_EXT (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', WIDGET varchar(200) not null comment '组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', DEF_VAL varchar(100) comment '默认值', SORT integer not null comment '排序', SQL_TYPE integer not null comment 'SQL片段脚本类型', SQL_SCRIPT longtext not null comment 'SQL片段脚本', DESCRIPTION varchar(500) comment '描述', NAME varchar(100) comment '表单字段名', primary key (ID)) comment='动态表查询条件(高级)视图模块' ENGINE=InnoDB;
create table VW_DYN_SUB_SYS (SUB_KEY varchar(100) not null comment '唯一主键', VIEW_KEY varchar(100) not null comment '视图KEY', NAME varchar(100) not null comment '界面系统标签NAME(用于事件加载)', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '标签样式', SORT integer not null comment '排序', PRI varchar(100) not null comment '展示权限', primary key (SUB_KEY)) comment='系统内置视图子表标签' ENGINE=InnoDB;
create table VW_DYN_SUB_VIEW (SUB_KEY varchar(100) not null comment '唯一主键', VIEW_KEY varchar(100) not null comment '视图KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '标签样式', SORT integer not null comment '排序', ACTION varchar(500) not null comment '目标地址', PARAM_TYPE integer comment '控件动态参数', PARAM_SCRIPT longtext comment '控件动态参数', PRI varchar(100) not null comment '展示权限', primary key (SUB_KEY)) comment='视图子表标签' ENGINE=InnoDB;
create table VW_DYN_TABLE (VIEW_KEY varchar(100) not null comment '主键KEY', NAME varchar(100) not null comment '表名', LOG_TABLE varchar(100) comment '操作日志表', BUSI_NAME varchar(200) not null comment '展示名', SORT_NAME varchar(100) not null comment '排序字段', DIR varchar(100) not null comment '排序方向', COL integer not null comment '展示分列数量', PAGE_LIMIT integer comment '每页数量', INIT_QUERY integer default 1 not null comment '初始化查询', LIST_JS_TYPE integer comment '客户端脚本类型', LIST_JS_SCRIPT longtext comment '客户端脚本', FORM_JS_TYPE integer comment '客户端脚本类型', FORM_JS_SCRIPT longtext comment '客户端脚本', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', DESCRIPTION varchar(500) comment '描述', primary key (VIEW_KEY)) comment='动态表视图模块' ENGINE=InnoDB;
create table VW_DYN_WEIXIN (VIEW_KEY varchar(100) not null comment '主键KEY', LIST_MODE integer not null comment '列表展示模式(0:纯文;1:图文)', URL_MODE integer default 0 not null comment '数据展示形式(0:查看优先;1:编辑优先)', TITLE_TYPE integer comment '标题脚本类型', TITLE_SCRIPT longtext comment '标题脚本', IMG_TYPE integer comment '图标区域脚本类型', IMG_SCRIPT longtext comment '图标区域脚本', DES_TYPE integer comment '描述区域脚本类型', DES_SCRIPT longtext comment '描述区域脚本', DATE_TYPE integer comment '时间区域脚本类型', DATE_SCRIPT longtext comment '时间区域脚本', PRI varchar(100) not null comment '权限功能点', primary key (VIEW_KEY)) comment='动态表视图微信配置' ENGINE=InnoDB;
create table VW_FLOW_BASIC (VIEW_KEY varchar(100) not null comment '主键KEY', PD_KEY varchar(100) not null comment '流程KEY', TABLE_NAME varchar(100) not null comment '关联订单表名', INIT_QUERY integer default 1 not null comment '初始化查询', HISTORY_TABLE_NAME varchar(100) comment '关联订单历史表名', OPINION_TABLE_NAME varchar(100) comment '关联审批意见表名', BUSI_NAME varchar(200) not null comment '展示名', COL integer not null comment '展示分列数量', SORT_NAME varchar(100) not null comment '排序字段', DIR varchar(100) not null comment '排序方向', PAGE_LIMIT integer comment '每页数量', ORD_ID_TYPE integer not null comment '订单号生成规则-脚本类型', ORD_ID_SCRIPT longtext not null comment '订单号生成规则-脚本', MSG_TYPE varchar(100) comment '消息通知开关(多选,分号隔离;MAIL:邮件;WX:微信;)', MAIL_SUBJECT_TYPE integer comment '邮件通知-标题-脚本类型', MAIL_SUBJECT_SCRIPT longtext comment '邮件通知-标题-脚本', MAIL_CONTENT_TYPE integer comment '邮件通知-内容-脚本类型', MAIL_CONTENT_SCRIPT longtext comment '邮件通知-内容-脚本', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', DESCRIPTION varchar(500) comment '描述', primary key (VIEW_KEY)) comment='工作流基础视图' ENGINE=InnoDB;
create table VW_FLOW_BASIC_BTN_ITEM (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', ACTION varchar(500) not null comment '目标地址', OPEN_TYPE integer default 1 not null comment '打开方式', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CONFIRM_MSG varchar(1000) comment '确认信息(提示框)', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='明细按钮(循环中每行展示)' ENGINE=InnoDB;
create table VW_FLOW_BASIC_BTN_SUMMARY (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE_CLASS varchar(100) not null comment '对齐方式(left/right)', ICON varchar(100) not null comment '图标', ACTION varchar(500) not null comment '目标地址', OPEN_TYPE integer default 1 not null comment '打开方式', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CONFIRM_MSG varchar(1000) comment '确认信息(提示框)', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='汇总按钮(表格底部展示)' ENGINE=InnoDB;
create table VW_FLOW_BASIC_BTN_SYS (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', TYPE integer not null comment '按钮类型(1:明细按钮/2:汇总按钮)', NAME varchar(100) not null comment '界面JS按钮名(编程装载事件,也用来与自定义按钮区分)', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) not null comment '对齐方式(left/right)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', PRI varchar(100) comment '权限功能点', primary key (ID)) comment='系统内置按钮' ENGINE=InnoDB;
create table VW_FLOW_BASIC_COLUMN_LINE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '视图主键', PIXEL_KEY varchar(100) not null comment '扩展标识键', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', EXPAND_FLAG integer not null comment '是否展开', SORT integer not null comment '排序', PRI varchar(100) not null comment '展示权限', primary key (ID)) comment='字段分割线' ENGINE=InnoDB;
create table VW_FLOW_BASIC_COLUMN_SHOW (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '视图主键', PIXEL_KEY varchar(100) not null comment '扩展标识键', BUSI_NAME varchar(200) not null comment '展示名', WHOLE integer not null comment '占据全行', STYLE varchar(500) comment '列样式', SORT_FIELD varchar(100) comment '排序字段', CONTENT_TYPE integer not null comment '内容脚本类型', CONTENT_SCRIPT longtext not null comment '内容脚本', SORT integer not null comment '排序', LIST_SORT integer default 0 not null comment '列表页排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='工作流基础视图展示字段模块' ENGINE=InnoDB;
create table VW_FLOW_BASIC_EXEC_AFTER (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='后置处理器' ENGINE=InnoDB;
create table VW_FLOW_BASIC_EXEC_BEFORE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='前置处理器' ENGINE=InnoDB;
create table VW_FLOW_BASIC_EXEC_PREPARE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', VAR varchar(100) not null comment '变量(别名)', SORT integer not null comment '排序', primary key (ID)) comment='数据准备处理器' ENGINE=InnoDB;
create table VW_FLOW_BASIC_LIMIT (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', DESCRIPTION varchar(500) not null comment '描述', SQL_TYPE integer not null comment '内容脚本类型', SQL_SCRIPT longtext not null comment '内容脚本', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='数据约束规则' ENGINE=InnoDB;
create table VW_FLOW_BASIC_QUERY (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', NAME varchar(100) not null comment '字段名', BUSI_NAME varchar(200) not null comment '展示名', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', DEF_VAL varchar(100) comment '默认值', SORT integer not null comment '排序', primary key (ID)) comment='查询条件视图模块' ENGINE=InnoDB;
create table VW_FLOW_BASIC_QUERY_EXT (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', WIDGET varchar(200) not null comment '组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', DEF_VAL varchar(100) comment '默认值', SORT integer not null comment '排序', SQL_TYPE integer not null comment 'SQL片段脚本类型', SQL_SCRIPT longtext not null comment 'SQL片段脚本', DESCRIPTION varchar(500) comment '描述', primary key (ID)) comment='查询条件(高级)视图模块' ENGINE=InnoDB;
create table VW_FLOW_BASIC_SUB_SYS (SUB_KEY varchar(100) not null comment '唯一主键', VIEW_KEY varchar(100) not null comment '视图KEY', NAME varchar(100) not null comment '界面系统标签NAME(用于事件加载)', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '标签样式', SORT integer not null comment '排序', PRI varchar(100) not null comment '展示权限', primary key (SUB_KEY)) comment='子表' ENGINE=InnoDB;
create table VW_FLOW_BASIC_SUB_VIEW (SUB_KEY varchar(100) not null comment '唯一主键', VIEW_KEY varchar(100) not null comment '视图KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '标签样式', SORT integer not null comment '排序', ACTION varchar(500) not null comment '目标地址', PARAM_TYPE integer comment '控件动态参数', PARAM_SCRIPT longtext comment '控件动态参数', PRI varchar(100) not null comment '展示权限', primary key (SUB_KEY)) comment='视图子表标签' ENGINE=InnoDB;
create table VW_FLOW_BASIC_WEIXIN (VIEW_KEY varchar(100) not null comment '主键KEY', LIST_MODE integer not null comment '列表展示模式(0:纯文;1:图文)', URL_MODE integer default 1 not null comment '数据展示形式(0:查看优先;1:编辑优先)', TITLE_TYPE integer comment '标题脚本类型', TITLE_SCRIPT longtext comment '标题脚本', IMG_TYPE integer comment '图标区域脚本类型', IMG_SCRIPT longtext comment '图标区域脚本', DES_TYPE integer comment '描述区域脚本类型', DES_SCRIPT longtext comment '描述区域脚本', DATE_TYPE integer comment '时间区域脚本类型', DATE_SCRIPT longtext comment '时间区域脚本', PRI varchar(100) not null comment '权限功能点', primary key (VIEW_KEY)) comment='工作流视图微信配置' ENGINE=InnoDB;
create table VW_NOTE_LIMIT (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', DESCRIPTION varchar(500) not null comment '描述', SQL_TYPE integer not null comment '内容脚本类型', SQL_SCRIPT longtext not null comment '内容脚本', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='动态表数据约束规则' ENGINE=InnoDB;
create table VW_NOTE_TABLE (VIEW_KEY varchar(100) not null comment '主键KEY', TABLE_NAME varchar(100) not null comment '表名', BUSI_NAME varchar(200) not null comment '展示名', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', primary key (VIEW_KEY)) comment='公告视图模块' ENGINE=InnoDB;
create table VW_REPORT (VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', LIST_JS_TYPE integer comment '客户端脚本类型', LIST_JS_SCRIPT longtext comment '客户端脚本', MAIN_SQL_TYPE integer not null comment '主SQL语句(脚本类型)', MAIN_SQL_SCRIPT longtext not null comment '主SQL语句', PK_TYPE integer comment '唯一键传值(脚本类型)', PK_SCRIPT longtext comment '唯一键传值', PK_SQL_TYPE integer comment '唯一键SQL(脚本类型)', PK_SQL_SCRIPT longtext comment '唯一键SQL', ORDER_BY varchar(200) comment '排序语句', COL integer not null comment '分列数量', INIT_QUERY integer not null comment '初始化查询', SORT integer default 0 not null comment '默认标签排序', PAGE_FLAG integer not null comment '是否分页', PAGE_LIMIT integer comment '每页数量', SUMMARY_FLAG integer not null comment '是否展示汇集行', DESCRIPTION varchar(500) comment '描述', DB_KEY varchar(100) comment '使用数据源', primary key (VIEW_KEY)) comment='报表明细视图模块' ENGINE=InnoDB;
create table VW_REPORT_BTN_ITEM (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', ACTION varchar(500) not null comment '目标地址', OPEN_TYPE integer default 1 not null comment '打开方式', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CONFIRM_MSG varchar(1000) comment '确认信息(提示框)', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='明细按钮(循环中每行展示)' ENGINE=InnoDB;
create table VW_REPORT_BTN_SUMMARY (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE_CLASS varchar(100) not null comment '对齐方式(left/right)', ICON varchar(100) not null comment '图标', ACTION varchar(500) not null comment '目标地址', OPEN_TYPE integer default 1 not null comment '打开方式', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CONFIRM_MSG varchar(1000) comment '确认信息(提示框)', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='汇总按钮(表格底部展示)' ENGINE=InnoDB;
create table VW_REPORT_BTN_SYS (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', TYPE integer not null comment '按钮类型(1:明细按钮/2:汇总按钮)', NAME varchar(100) not null comment '界面JS按钮名(编程装载事件,也用来与自定义按钮区分)', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) not null comment '对齐方式(left/right)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', PRI varchar(100) not null comment '权限功能点', primary key (ID)) comment='系统内置按钮' ENGINE=InnoDB;
create table VW_REPORT_COLUMN_LINE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '视图主键', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', EXPAND_FLAG integer not null comment '是否展开', SORT integer not null comment '排序', PRI varchar(100) not null comment '展示权限', primary key (ID)) comment='字段分割线' ENGINE=InnoDB;
create table VW_REPORT_COLUMN_SHOW (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '列样式', WHOLE integer not null comment '占据全行', SORT_FIELD varchar(100) comment '排序字段', CONTENT_TYPE integer not null comment '内容脚本类型', CONTENT_SCRIPT longtext not null comment '内容脚本', SUMMARY_CONTENT_TYPE integer comment '汇集内容脚本类型', SUMMARY_CONTENT_SCRIPT longtext comment '汇集内容脚本', SORT integer not null comment '排序', LIST_SORT integer not null comment '列表页排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='动态表展示列视图模块' ENGINE=InnoDB;
create table VW_REPORT_EXEC_PREPARE (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', VAR varchar(100) not null comment '变量(别名)', SORT integer not null comment '排序', primary key (ID)) comment='数据准备处理器' ENGINE=InnoDB;
create table VW_REPORT_LIMIT (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', DESCRIPTION varchar(500) not null comment '描述', SQL_TYPE integer not null comment '内容脚本类型', SQL_SCRIPT longtext not null comment '内容脚本', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='动态表数据约束规则' ENGINE=InnoDB;
create table VW_REPORT_QUERY (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', NAME varchar(100) comment '表单字段名', WIDGET varchar(200) not null comment '组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', DEF_VAL varchar(100) comment '默认值', SORT integer not null comment '排序', SQL_TYPE integer not null comment 'SQL片段脚本类型', SQL_SCRIPT longtext not null comment 'SQL片段脚本', DESCRIPTION varchar(500) comment '描述', primary key (ID)) comment='动态表查询条件(高级)视图模块' ENGINE=InnoDB;
create table VW_REPORT_SUB_VIEW (SUB_KEY varchar(100) not null comment '唯一主键', VIEW_KEY varchar(100) not null comment '视图KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '标签样式', SORT integer not null comment '排序', ACTION varchar(500) not null comment '目标地址', PARAM_TYPE integer comment '控件动态参数', PARAM_SCRIPT longtext comment '控件动态参数', PRI varchar(100) not null comment '展示权限', primary key (SUB_KEY)) comment='视图子表标签' ENGINE=InnoDB;
create table VW_REPORT_WEIXIN (VIEW_KEY varchar(100) not null comment '主键KEY', LIST_MODE integer not null comment '列表展示模式(0:纯文;1:图文)', URL_MODE integer default 0 not null comment '数据展示形式(0:查看优先;1:编辑优先)', TITLE_TYPE integer comment '标题脚本类型', TITLE_SCRIPT longtext comment '标题脚本', IMG_TYPE integer comment '图标区域脚本类型', IMG_SCRIPT longtext comment '图标区域脚本', DES_TYPE integer comment '描述区域脚本类型', DES_SCRIPT longtext comment '描述区域脚本', DATE_TYPE integer comment '时间区域脚本类型', DATE_SCRIPT longtext comment '时间区域脚本', PRI varchar(100) not null comment '权限功能点', primary key (VIEW_KEY)) comment='报表视图微信配置' ENGINE=InnoDB;
create table VW_URL (VIEW_KEY varchar(100) not null comment '主键KEY', LOCK_FLAG integer default 0 not null comment '是否锁定(锁定则无法编辑)', VIEW_CLASS varchar(100) not null comment '对应模块', LOGIN_TYPE integer default 1 not null comment '访问视图的登录模式;1:系统用户登陆;0:无需登录', CREATE_UID varchar(100) not null comment '创建人', DESCRIPTION varchar(500) comment '描述', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', primary key (VIEW_KEY)) comment='动态视图配置' ENGINE=InnoDB;
create table VW_VIEWER (VIEW_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', RESULT_TYPE integer not null comment '输出结果类型(1:ftl,2:excel,3:pdf)', TEMPLATE_FILE mediumblob comment '模板文件', FILE_TYPE integer comment '下载类型', FILE_SCRIPT longtext comment '下载内容', TEXT_TYPE integer comment '文本类型', TEXT_SCRIPT longtext comment '文本内容', MSG_TYPE integer comment '消息类型', MSG_SCRIPT longtext comment '消息内容', URL_TYPE integer comment '跳转网址类型', URL_SCRIPT longtext comment '跳转网址内容', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', TEMP_FILE_TYPE integer default 0 comment '模板类型(0:模板文件,1:模板文件路径;默认0)', TEMP_FILE_PATH varchar(200) comment '模板文件路径', primary key (VIEW_KEY)) comment='Viewer视图-文件模板展示' ENGINE=InnoDB;
create table VW_VIEWER_VAR (ID bigint not null comment '自动主键', VIEW_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', VAR varchar(100) not null comment '变量(别名)', SORT integer not null comment '排序', primary key (ID)) comment='展示变量' ENGINE=InnoDB;
create table WDG_BASE (WIDGET_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', JS_TYPE integer comment '客户端脚本类型', JS_SCRIPT longtext comment '客户端脚本', MAIN_SQL_TYPE integer not null comment '主SQL语句(脚本类型)', MAIN_SQL_SCRIPT longtext not null comment '主SQL语句', ORDER_BY varchar(200) comment '排序语句', WIDTH integer comment '弹出框宽度', PAGE_LIMIT integer comment '每页数量', INIT_QUERY integer not null comment '初始化查询', CREATE_UID varchar(100) not null comment '创建人', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', DESCRIPTION varchar(500) comment '描述', primary key (WIDGET_KEY)) comment='数据控件' ENGINE=InnoDB;
create table WDG_BASE_COLUMN_FORM (ID bigint not null comment '自动主键', WIDGET_KEY varchar(100) not null comment '主键KEY', NAME varchar(200) not null comment '表单名', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '列样式', CONTENT_TYPE integer not null comment '内容脚本类型', CONTENT_SCRIPT longtext not null comment '内容脚本', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', EXEC_TYPE integer comment '前置处理脚本类型', EXEC_SCRIPT longtext comment '前置处理脚本', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', EDIT_PRI varchar(100) not null comment '编辑权限', primary key (ID)) comment='表单列' ENGINE=InnoDB;
create table WDG_BASE_COLUMN_SHOW (ID bigint not null comment '自动主键', IN_WAIT integer not null comment '待选页', IN_SELECTED integer not null comment '已选页', IN_RESULT integer not null comment '结果页', WIDGET_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', STYLE varchar(500) comment '列样式', CONTENT_TYPE integer not null comment '内容脚本类型', CONTENT_SCRIPT longtext not null comment '内容脚本', SORT_FIELD varchar(200) comment '排序字段', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='展示列' ENGINE=InnoDB;
create table WDG_BASE_LIMIT (ID bigint not null comment '自动主键', WIDGET_KEY varchar(100) not null comment '主键KEY', DESCRIPTION varchar(500) not null comment '描述', SQL_TYPE integer not null comment '内容脚本类型', SQL_SCRIPT longtext not null comment '内容脚本', SORT integer not null comment '排序', PRI varchar(100) not null comment '权限资源', primary key (ID)) comment='数据约束规则' ENGINE=InnoDB;
create table WDG_BASE_QUERY (ID bigint not null comment '自动主键', WIDGET_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', WIDGET varchar(200) not null comment '组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', DEF_VAL varchar(100) comment '默认值', SORT integer not null comment '排序', SQL_TYPE integer not null comment 'SQL片段脚本类型', SQL_SCRIPT longtext not null comment 'SQL片段脚本', DESCRIPTION varchar(500) comment '描述', NAME varchar(100) comment '表单字段名', primary key (ID)) comment='查询条件(高级)' ENGINE=InnoDB;
create table WDG_COMBO (WIDGET_KEY varchar(100) not null comment '主键KEY', CODE_TYPE integer not null comment 'CODE逻辑(脚本类型)', CODE_SCRIPT longtext not null comment 'CODE逻辑(脚本)', NAME_TYPE integer not null comment 'NAME逻辑(脚本类型)', NAME_SCRIPT longtext not null comment 'NAME逻辑(脚本)', PK_SQL_TYPE integer comment '唯一值SQL片段(脚本类型)', PK_SQL_SCRIPT longtext comment '唯一值SQL片段(脚本)', primary key (WIDGET_KEY)) comment='高级选择控件' ENGINE=InnoDB;
create table WDG_DETAIL (WIDGET_KEY varchar(100) not null comment '主键KEY', PK_TYPE integer not null comment '主键逻辑(脚本类型)', PK_SCRIPT longtext not null comment '主键逻辑(脚本)', ALLOW_ADD integer not null comment '是否允许新增', ALLOW_DELETE integer not null comment '是否允许删除', PAGE_FLAG integer default 0 not null comment '是否允许分页', BATCH_FLAG integer not null comment '是否批处理', SUMARRY_TYPE integer comment '汇总(脚本类型)', SUMARRY_SCRIPT longtext comment '汇总(脚本)', primary key (WIDGET_KEY)) comment='明细控件' ENGINE=InnoDB;
create table WDG_DETAIL_COLUMN_BATCH (ID bigint not null comment '自动主键', WIDGET_KEY varchar(100) not null comment '主键KEY', NAME varchar(100) not null comment '字段名', BUSI_NAME varchar(100) not null comment '展示名', EXAMPLE varchar(200) comment '例子', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='批量字段' ENGINE=InnoDB;
create table WDG_DETAIL_EXEC (ID bigint not null comment '自动主键', WIDGET_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='处理器' ENGINE=InnoDB;
create table WDG_TEMPLATE (WIDGET_KEY varchar(100) not null comment '主键KEY', BUSI_NAME varchar(200) not null comment '展示名', TEMPLATE_FILE mediumblob comment '模板文件', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', CREATE_UID varchar(100) not null comment '创建人', DESCRIPTION varchar(500) comment '描述', primary key (WIDGET_KEY)) comment='模板控件' ENGINE=InnoDB;
create table WDG_TEMPLATE_VAR (ID bigint not null comment '自动主键', WIDGET_KEY varchar(100) not null comment '主键KEY', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', VAR varchar(100) not null comment '变量(别名)', SORT integer not null comment '排序', primary key (ID)) comment='展示变量' ENGINE=InnoDB;
create table WF_END_EVENT (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', STATE_TYPE integer not null comment '完成模式', DESCRIPTION varchar(500) comment '描述', primary key (PD_ID, ACTIVITY_ID)) comment='结束节点配置表' ENGINE=InnoDB;
create table WF_EXCLUSIVE_GATEWAY (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', DESCRIPTION varchar(500) comment '描述', primary key (PD_ID, ACTIVITY_ID)) comment='判断节点配置' ENGINE=InnoDB;
create table WF_EXCLUSIVE_GATEWAY_DECIDE (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', FLOW_ID varchar(100) not null comment '连线ID', DECIDE_TYPE integer not null comment '脚本类型', DECIDE_SCRIPT longtext not null comment '脚本', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', primary key (ID)) comment='判断条件' ENGINE=InnoDB;
create table WF_INCLUSIVE_GATEWAY (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', DESCRIPTION varchar(500) comment '描述', primary key (PD_ID, ACTIVITY_ID)) comment='包容网关节点配置' ENGINE=InnoDB;
create table WF_INCLUSIVE_GATEWAY_DECIDE (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', FLOW_ID varchar(100) not null comment '连线ID', DECIDE_TYPE integer not null comment '脚本类型', DECIDE_SCRIPT longtext not null comment '脚本', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', primary key (ID)) comment='判断条件' ENGINE=InnoDB;
create table WF_PD (PD_ID varchar(100) not null comment '流程定义ID', BASIC_VIEW_KEY varchar(200) not null comment '基础视图主键', UPDATE_DATE datetime not null comment '更新时间', CREATE_DATE datetime not null comment '创建时间', DESCRIPTION varchar(500) comment '描述', primary key (PD_ID)) comment='流程定义配置表' ENGINE=InnoDB;
create table WF_SERVICE_TASK (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', DESCRIPTION varchar(500) comment '描述', primary key (PD_ID, ACTIVITY_ID)) comment='自动服务配置' ENGINE=InnoDB;
create table WF_SERVICE_TASK_LOGIC (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', LOGIC_TYPE integer not null comment '处理逻辑(脚本类型)', LOGIC_SCRIPT longtext not null comment '处理逻辑(脚本)', ERROR_TYPE integer not null comment '错误处理方式', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='自动处理逻辑' ENGINE=InnoDB;
create table WF_START_EVENT (PD_ID varchar(100) not null comment '流程定义ID', JS_TYPE integer comment '客户端脚本类型', JS_SCRIPT longtext comment '客户端脚本', DESCRIPTION varchar(500) comment '描述', primary key (PD_ID)) comment='开始节点配置' ENGINE=InnoDB;
create table WF_START_EVENT_BTN_SAVE (PD_ID varchar(100) not null comment '流程定义ID', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) comment '对齐方式(left/right/center)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', CHECK_TYPE integer not null comment '展示条件(脚本类型)', CHECK_SCRIPT longtext not null comment '展示条件(脚本)', LOADING integer not null comment '高级进度条', primary key (PD_ID)) comment='节点按钮-保存' ENGINE=InnoDB;
create table WF_START_EVENT_BTN_START (PD_ID varchar(100) not null comment '流程定义ID', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) comment '对齐方式(left/right/center)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', CONFIRM_TYPE integer comment '确认信息(脚本类型)', CONFIRM_SCRIPT longtext comment '确认信息(脚本)', OPINION_FALG integer default 0 not null comment '是否使用审批意见', CHECK_TYPE integer not null comment '展示条件(脚本类型)', CHECK_SCRIPT longtext not null comment '展示条件(脚本)', QUICK_OPINION_TYPE integer comment '快速回复(脚本类型)', QUICK_OPINION_SCRIPT longtext comment '快速回复(脚本)', LOADING integer not null comment '高级进度条', primary key (PD_ID)) comment='节点按钮-启动' ENGINE=InnoDB;
create table WF_START_EVENT_COLUMN_EXTEND (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', PIXEL_KEY varchar(100) not null comment '扩展标识键', SHOW_FLAG integer not null comment '是否展示', DESCRIPTION varchar(500) comment '描述', CONTENT_TYPE integer comment '内容脚本类型', CONTENT_SCRIPT longtext comment '内容脚本', SORT integer not null comment '排序', primary key (ID)) comment='继承字段' ENGINE=InnoDB;
create table WF_START_EVENT_COLUMN_FORM (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', NAME varchar(100) not null comment '字段名', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', SHOW_CONTENT_TYPE integer comment '展示内容脚本类型', SHOW_CONTENT_SCRIPT longtext comment '展示内容脚本', CONTENT_TYPE integer comment '内容脚本类型', CONTENT_SCRIPT longtext comment '内容脚本', WHOLE integer not null comment '占据全行', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', SORT integer not null comment '排序', EXEC_TYPE integer comment '前置处理脚本类型', EXEC_SCRIPT longtext comment '前置处理脚本', DECIDE_TYPE integer not null comment '成立条件-脚本类型', DECIDE_SCRIPT longtext not null comment '成立条件-脚本', EDIT_DECIDE_TYPE integer not null comment '可编辑条件-脚本类型', EDIT_DECIDE_SCRIPT longtext not null comment '可编辑条件-脚本', primary key (ID)) comment='表单字段' ENGINE=InnoDB;
create table WF_START_EVENT_COLUMN_LINE (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', EXPAND_FLAG integer not null comment '是否展开', SORT integer not null comment '排序', DECIDE_TYPE integer not null comment '成立条件-脚本类型', DECIDE_SCRIPT longtext not null comment '成立条件-脚本', primary key (ID)) comment='字段分割线' ENGINE=InnoDB;
create table WF_START_EVENT_EXEC_AFTER (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='后置处理器' ENGINE=InnoDB;
create table WF_START_EVENT_EXEC_BEFORE (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='前置处理器' ENGINE=InnoDB;
create table WF_START_EVENT_SUB_EXTEND (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', SUB_KEY varchar(100) not null comment '扩展标识键', SHOW_FLAG integer not null comment '是否展示', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='继承子表' ENGINE=InnoDB;
create table WF_USER_TASK (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', JS_TYPE integer comment '客户端脚本类型', JS_SCRIPT longtext comment '客户端脚本', NOTIFY_TYPE varchar(100) comment '通知触发类型(多选,1:任务接收人;2:转发接收人;3:群组任务触发)', DESCRIPTION varchar(500) comment '描述', primary key (PD_ID, ACTIVITY_ID)) comment='用户任务配置' ENGINE=InnoDB;
create table WF_USER_TASK_ASSIGNEE (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', DECIDE_TYPE integer not null comment '成立条件-脚本类型', DECIDE_SCRIPT longtext not null comment '成立条件-脚本', ALLOCATE_TYPE integer not null comment '分配方式', UNIQUE_FLAG integer default 0 not null comment '是否独占', BATCH_NUM integer not null comment '批次', UID_TYPE integer comment '分配用户-脚本类型', UID_SCRIPT longtext comment '分配用户-脚本', ROLE_TYPE integer comment '分配角色-脚本类型', ROLE_SCRIPT longtext comment '分配角色-脚本', GROUP_TYPE integer comment '分配组织-脚本类型', GROUP_SCRIPT longtext comment '分配组织-脚本', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', primary key (ID)) comment='用户节点人员分配规则' ENGINE=InnoDB;
create table WF_USER_TASK_BTN (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', FLOW_ID varchar(100) not null comment '连线ID', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) comment '对齐方式(left/right/center)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', CHECK_TYPE integer not null comment '展示条件(脚本类型)', CHECK_SCRIPT longtext not null comment '展示条件(脚本)', ENABLED_TIP_TYPE integer comment '按钮可用时提示(脚本类型)', ENABLED_TIP_SCRIPT longtext comment '按钮可用时提示(脚本)', DISABLED_TIP_TYPE integer comment '按钮不可用时提示(脚本类型)', DISABLED_TIP_SCRIPT longtext comment '按钮不可用时提示(脚本)', CONFIRM_TYPE integer comment '确认信息(脚本类型)', CONFIRM_SCRIPT longtext comment '确认信息(脚本)', OPINION_FALG integer default 0 not null comment '是否使用审批意见', QUICK_OPINION_TYPE integer comment '快速回复(脚本类型)', QUICK_OPINION_SCRIPT longtext comment '快速回复(脚本)', LOADING integer not null comment '高级进度条', primary key (PD_ID, ACTIVITY_ID, FLOW_ID)) comment='节点按钮' ENGINE=InnoDB;
create table WF_USER_TASK_BTN_FORWARD (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', BTN_KEY varchar(100) not null comment 'UUID', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) comment '对齐方式(left/right/center)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', CHECK_TYPE integer not null comment '展示条件(脚本类型)', CHECK_SCRIPT longtext not null comment '展示条件(脚本)', QUICK_OPINION_TYPE integer comment '快速回复(脚本类型)', QUICK_OPINION_SCRIPT longtext comment '快速回复(脚本)', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', WIDGET_ENABLE_TYPE integer not null comment '控件可用条件', WIDGET_ENABLE_SCRIPT longtext not null comment '控件可用条件', WIDGET_VAL_TYPE integer comment '控件默认值', WIDGET_VAL_SCRIPT longtext comment '控件默认值', primary key (PD_ID, ACTIVITY_ID, BTN_KEY)) comment='节点按钮-转办' ENGINE=InnoDB;
create table WF_USER_TASK_BTN_SAVE (PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '节点ID', BUSI_NAME varchar(200) not null comment '展示名', ICON varchar(100) not null comment '图标', STYLE_CLASS varchar(100) comment '对齐方式(left/right/center)', SORT integer not null comment '排序', DESCRIPTION varchar(500) comment '描述', CHECK_TYPE integer not null comment '展示条件(脚本类型)', CHECK_SCRIPT longtext not null comment '展示条件(脚本)', LOADING integer not null comment '高级进度条', primary key (PD_ID, ACTIVITY_ID)) comment='节点按钮-保存' ENGINE=InnoDB;
create table WF_USER_TASK_COLUMN_EXTEND (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', PIXEL_KEY varchar(100) not null comment '扩展标识键', SHOW_FLAG integer not null comment '是否展示', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='继承字段' ENGINE=InnoDB;
create table WF_USER_TASK_COLUMN_FORM (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', NAME varchar(100) not null comment '字段名', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', SHOW_CONTENT_TYPE integer comment '展示内容脚本类型', SHOW_CONTENT_SCRIPT longtext comment '展示内容脚本', CONTENT_TYPE integer comment '内容脚本类型', CONTENT_SCRIPT longtext comment '内容脚本', WHOLE integer not null comment '占据全行', WIDGET varchar(200) not null comment '绑定组件', WIDGET_PARAM_TYPE integer comment '控件动态参数', WIDGET_PARAM_SCRIPT longtext comment '控件动态参数', SORT integer not null comment '排序', EXEC_TYPE integer comment '前置处理脚本类型', EXEC_SCRIPT longtext comment '前置处理脚本', DECIDE_TYPE integer not null comment '成立条件-脚本类型', DECIDE_SCRIPT longtext not null comment '成立条件-脚本', EDIT_DECIDE_TYPE integer not null comment '可编辑条件-脚本类型', EDIT_DECIDE_SCRIPT longtext not null comment '可编辑条件-脚本', primary key (ID)) comment='表单字段' ENGINE=InnoDB;
create table WF_USER_TASK_COLUMN_LINE (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', BUSI_NAME varchar(200) not null comment '展示名', TIP_TYPE integer comment '提示信息(脚本类型)', TIP_SCRIPT longtext comment '提示信息(脚本)', EXPAND_FLAG integer not null comment '是否展开', SORT integer not null comment '排序', DECIDE_TYPE integer not null comment '成立条件-脚本类型', DECIDE_SCRIPT longtext not null comment '成立条件-脚本', primary key (ID)) comment='字段分割线' ENGINE=InnoDB;
create table WF_USER_TASK_EXEC_AFTER (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', FLOW_ID varchar(100) not null comment '连线ID', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='后置处理器' ENGINE=InnoDB;
create table WF_USER_TASK_EXEC_BEFORE (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', FLOW_ID varchar(100) not null comment '连线ID', EXEC_TYPE integer not null comment '脚本类型', EXEC_SCRIPT longtext not null comment '脚本', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='前置处理器' ENGINE=InnoDB;
create table WF_USER_TASK_SUB_EXTEND (ID bigint not null comment '自动主键', PD_ID varchar(100) not null comment '流程定义ID', ACTIVITY_ID varchar(100) not null comment '流程定义ID', SUB_KEY varchar(100) not null comment '扩展标识键', SHOW_FLAG integer not null comment '是否展示', DESCRIPTION varchar(500) comment '描述', SORT integer not null comment '排序', primary key (ID)) comment='继承子表' ENGINE=InnoDB;
create table WX_AGENT (AGENT_KEY varchar(100) not null comment '主键KEY', CREATE_UID varchar(100) not null comment '创建人', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', PUBLIC_DATE datetime comment '发布时间', STATUS integer not null comment '状态.0:已创建未连接;1:已对接;2:应用不存在', AGENT_ID integer not null comment '企业号agentId', TOKEN varchar(200) not null comment 'Token', ENCODING_AES_KEY varchar(200) not null comment 'EncodingAESKey', AGENT_SECRET varchar(200) comment 'Secret', TITLE varchar(200) comment '名称', DESCRIPTION longtext comment '描述', LOGO_URL longtext comment 'logo地址', REPORT_USER_ENTER integer not null comment '用户进入事件开关', REPORT_USER_CHANGE integer not null comment '是否上报用户状态变化', REPORT_LOCATION_FLAG integer not null comment '是否接受用户地址上报', CLOSE_FLAG integer not null comment '是否被禁用', primary key (AGENT_KEY)) comment='微信应用' ENGINE=InnoDB;
create table WX_AGENT_ENTER (AGENT_KEY varchar(100) not null comment '应用KEY', EVENT_FLAG integer not null comment '事件开关', LOG_FLAG integer not null comment '日志登记', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', LOG_TABLE varchar(100) comment '数据记录表', primary key (AGENT_KEY)) comment='[企业号]进入事件触发器' ENGINE=InnoDB;
create table WX_AGENT_LOCATION (AGENT_KEY varchar(100) not null comment '应用KEY', EVENT_FLAG integer not null comment '事件开关', LOG_FLAG integer not null comment '日志登记', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', LOG_TABLE varchar(100) comment '数据记录表', primary key (AGENT_KEY)) comment='[企业号]地址上报事件事件触发器' ENGINE=InnoDB;
create table WX_AGENT_MENU (MENU_KEY varchar(100) not null comment '菜单主键', AGENT_KEY varchar(100) not null comment 'Agent主键', PARENT_KEY varchar(100) comment '父菜单', BUSI_NAME varchar(100) not null comment '菜单名', DESCRIPTION longtext comment '描述', MENU_TYPE integer not null comment '菜单类型;0:只作为菜单(一级菜单用);1:超链接;10:仅推送事件;20:弹窗扫码;21:扫码推;30:拍照或相片选择;31:拍照;32:相片选择;40:地理位置选择', COMMAND_KEY varchar(100) comment '事件处理器主键', ACTION varchar(500) comment '连接地址', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', SORT integer not null comment '排序', primary key (MENU_KEY)) comment='[企业号]菜单配置' ENGINE=InnoDB;
create table WX_AGENT_MESSAGE (AGENT_KEY varchar(100) not null comment '应用KEY', EVENT_FLAG integer not null comment '事件开关', LOG_FLAG integer not null comment '日志登记', EVENT_TYPE varchar(100) comment '事件拦截的数据类型;TEXT;IMAGE;VOICE;VIDEO;SHORT_VIDEO;LOCATION', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', LOG_TYPE varchar(100) comment '登记的数据类型;TEXT;IMAGE;VOICE;VIDEO;SHORT_VIDEO;LOCATION', LOG_TABLE varchar(100) comment '数据记录表', primary key (AGENT_KEY)) comment='[企业号]对话框触发器' ENGINE=InnoDB;
create table WX_AGENT_SUBSCRIBE (AGENT_KEY varchar(100) not null comment '应用KEY', EVENT_FLAG integer not null comment '事件开关', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', primary key (AGENT_KEY)) comment='[企业号]用户关注事件触发器' ENGINE=InnoDB;
create table WX_AGENT_UNSUBSCRIBE (AGENT_KEY varchar(100) not null comment '应用KEY', EVENT_FLAG integer not null comment '事件开关', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', primary key (AGENT_KEY)) comment='[企业号]用户取消关注事件触发器' ENGINE=InnoDB;
create table WX_COMMAND (COMMAND_KEY varchar(100) not null comment '主键KEY', CREATE_UID varchar(100) not null comment '创建人', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', BUSI_NAME varchar(200) not null comment '展示名', DESCRIPTION longtext comment '描述', MP_FLAG integer default 0 not null comment '公众号类型.1:公众号mp;0:企业号qy.', SUPPORT_TYPE varchar(100) comment '支持类型.参考:WxCommandSupportType', LOGIC_TYPE integer not null comment '脚本', LOGIC_SCRIPT longtext not null comment '脚本', primary key (COMMAND_KEY)) comment='微信事件处理器' ENGINE=InnoDB;
create table WX_MP (MP_KEY varchar(100) not null comment '主键KEY', CREATE_UID varchar(100) not null comment '创建人', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', PUBLIC_DATE datetime comment '发布时间', STATUS integer not null comment '状态.0:已创建未连接;1:已对接', APP_ID varchar(200) not null comment '公众号appId', APP_SECRET varchar(200) comment '公众号appSecret', TOKEN varchar(200) not null comment 'Token', ENCODING_AES_KEY varchar(200) not null comment 'EncodingAESKey', TITLE varchar(200) not null comment '名称', LOGO_URL longtext comment 'logo地址', DESCRIPTION longtext comment '描述', VISITOR_TABLE varchar(200) not null comment '人员表', VISITOR_TAG_TABLE varchar(200) not null comment '人员标签表', GROUP_KEY varchar(200) comment '绑定系统组', ROLE_KEY varchar(200) comment '绑定系统角色', TEMPLATE_MSG_LOG_TABLE varchar(200) comment '模板消息发送日志表', ACCESS_TOKEN_URL varchar(300) comment 'accessToken获取地址', primary key (MP_KEY)) comment='微信公众号配置' ENGINE=InnoDB;
create table WX_MP_LOCATION (MP_KEY varchar(100) not null comment '公众号KEY', EVENT_FLAG integer not null comment '事件开关', LOG_FLAG integer not null comment '日志登记', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', LOG_TABLE varchar(100) comment '数据记录表', primary key (MP_KEY)) comment='[公众号]地址上报事件事件触发器' ENGINE=InnoDB;
create table WX_MP_MENU (MENU_KEY varchar(100) not null comment '菜单主键', MP_KEY varchar(100) not null comment 'MP主键', PARENT_KEY varchar(100) comment '父菜单', BUSI_NAME varchar(100) not null comment '菜单名', DESCRIPTION longtext comment '描述', MENU_TYPE integer not null comment '菜单类型;0:只作为菜单(一级菜单用);1:超链接;10:仅推送事件;20:弹窗扫码;21:扫码推;30:拍照或相片选择;31:拍照;32:相片选择;40:地理位置选择', COMMAND_KEY varchar(100) comment '事件处理器主键', ACTION varchar(500) comment '连接地址', PAGEPATH varchar(500) comment '小程序页面路径', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', SORT integer not null comment '排序', primary key (MENU_KEY)) comment='[公众号]菜单配置' ENGINE=InnoDB;
create table WX_MP_MESSAGE (MP_KEY varchar(100) not null comment '公众号KEY', EVENT_FLAG integer not null comment '事件开关', LOG_FLAG integer not null comment '日志登记', EVENT_TYPE varchar(100) comment '事件拦截的数据类型;TEXT;IMAGE;VOICE;VIDEO;SHORT_VIDEO;LOCATION', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', LOG_TYPE varchar(100) comment '登记的数据类型;TEXT;IMAGE;VOICE;VIDEO;SHORT_VIDEO;LOCATION', LOG_TABLE varchar(100) comment '数据记录表', primary key (MP_KEY)) comment='[公众号]对话框触发器' ENGINE=InnoDB;
create table WX_MP_ORDER (MP_KEY varchar(100) not null comment '公众号KEY', EVENT_FLAG integer not null comment '事件开关', LOG_FLAG integer not null comment '日志登记', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', LOG_TABLE varchar(100) comment '数据记录表', primary key (MP_KEY)) comment='[公众号]订单支付事件触发器' ENGINE=InnoDB;
create table WX_MP_PAY (MP_KEY varchar(100) not null comment '主键KEY', APP_ID varchar(200) not null comment '公众号appId', MCH_ID varchar(200) not null comment '商户ID', PAY_SECRET varchar(200) not null comment '支付API秘钥', CERT_PATH varchar(200) not null comment '证书路径', CERT_PASSWORD varchar(200) not null comment '证书密码', primary key (MP_KEY)) comment='[公众号]支付配置' ENGINE=InnoDB;
create table WX_MP_PAY_NOTIFY (MP_KEY varchar(100) not null comment '主键KEY', EVENT_FLAG integer not null comment '事件开关', LOG_FLAG integer not null comment '日志登记', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', LOG_TABLE varchar(100) comment '数据记录表', primary key (MP_KEY)) comment='[公众号]支付结果通知处理器' ENGINE=InnoDB;
create table WX_MP_SCANIN (MP_KEY varchar(100) not null comment '公众号KEY', EVENT_FLAG integer not null comment '事件开关', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', primary key (MP_KEY)) comment='[公众号]用户扫码进入事件触发器' ENGINE=InnoDB;
create table WX_MP_SUBSCRIBE (MP_KEY varchar(100) not null comment '公众号KEY', EVENT_FLAG integer not null comment '事件开关', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', primary key (MP_KEY)) comment='[公众号]用户关注事件触发器' ENGINE=InnoDB;
create table WX_MP_UNSUBSCRIBE (MP_KEY varchar(100) not null comment '公众号KEY', EVENT_FLAG integer not null comment '事件开关', COMMAND_KEY varchar(100) comment '事件处理器主键', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', DESCRIPTION longtext comment '描述', primary key (MP_KEY)) comment='[公众号]用户取消关注事件触发器' ENGINE=InnoDB;
create table WX_URL (URL_KEY varchar(100) not null comment 'URL主键', CREATE_UID varchar(100) not null comment '创建人', WX_TYPE integer not null comment '微信平台类型', WX_KEY varchar(20) comment '微信平台KEY', WX_SCOPE varchar(20) comment '微信跳转方式;snsapi_base;snsapi_userinfo', DESCRIPTION longtext comment '描述', ACTION varchar(500) comment '连接地址', PARAM_TYPE integer comment '动态参数', PARAM_SCRIPT longtext comment '动态参数', CREATE_DATE datetime not null comment '创建时间', UPDATE_DATE datetime not null comment '更新时间', primary key (URL_KEY)) comment='[微信]URL管理' ENGINE=InnoDB;
alter table VW_DYN_COLUMN add constraint VW_DYN_UNIQUE_KEY unique (VIEW_KEY, NAME);

-- Activiti 5.16.3 schema
create table ACT_GE_PROPERTY (
    NAME_ varchar(64),
    VALUE_ varchar(300),
    REV_ integer,
    primary key (NAME_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

insert into ACT_GE_PROPERTY
values ('schema.version', '5.16.3.0', 1);

insert into ACT_GE_PROPERTY
values ('schema.history', 'create(5.16.3.0)', 1);

insert into ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

create table ACT_GE_BYTEARRAY (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    BYTES_ LONGBLOB,
    GENERATED_ TINYINT,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RE_DEPLOYMENT (
    ID_ varchar(64),
    NAME_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    DEPLOY_TIME_ timestamp(3),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RE_MODEL (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    KEY_ varchar(255),
    CATEGORY_ varchar(255),
    CREATE_TIME_ timestamp(3) null,
    LAST_UPDATE_TIME_ timestamp(3) null,
    VERSION_ integer,
    META_INFO_ varchar(4000),
    DEPLOYMENT_ID_ varchar(64),
    EDITOR_SOURCE_VALUE_ID_ varchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_EXECUTION (
    ID_ varchar(64),
    REV_ integer,
    PROC_INST_ID_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SUPER_EXEC_ varchar(64),
    ACT_ID_ varchar(255),
    IS_ACTIVE_ TINYINT,
    IS_CONCURRENT_ TINYINT,
    IS_SCOPE_ TINYINT,
    IS_EVENT_SCOPE_ TINYINT,
    SUSPENSION_STATE_ integer,
    CACHED_ENT_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_JOB (
    ID_ varchar(64) NOT NULL,
  REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp(3) NULL,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp(3) NULL,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RE_PROCDEF (
    ID_ varchar(64) not null,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) not null,
    VERSION_ integer not null,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    DESCRIPTION_ varchar(4000),
    HAS_START_FORM_KEY_ TINYINT,
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_TASK (
    ID_ varchar(64),
    REV_ integer,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    TASK_DEF_KEY_ varchar(255),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    DELEGATION_ varchar(64),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp(3),
    DUE_DATE_ datetime(3),
    CATEGORY_ varchar(255),
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    FORM_KEY_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_IDENTITYLINK (
    ID_ varchar(64),
    REV_ integer,
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),    
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_VARIABLE (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    TASK_ID_ varchar(64),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(64),
    CONFIGURATION_ varchar(255),
    CREATED_ timestamp(3) not null DEFAULT CURRENT_TIMESTAMP(3),
    PROC_DEF_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_EVT_LOG (
    LOG_NR_ bigint auto_increment,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp(3) not null,
    USER_ID_ varchar(255),
    DATA_ LONGBLOB,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp(3) null,
    IS_PROCESSED_ tinyint default 0,
    primary key (LOG_NR_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_EXEC_BUSKEY on ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDX_TASK_CREATE on ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);
create index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE(TASK_ID_);
create index ACT_IDX_ATHRZ_PROCEDEF on ACT_RU_IDENTITYLINK(PROC_DEF_ID_);

alter table ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);

alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, TENANT_ID_);
    
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCINST 
    foreign key (PROC_INST_ID_) 
    references ACT_RU_EXECUTION (ID_) on delete cascade on update cascade;

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PARENT 
    foreign key (PARENT_ID_) 
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_SUPER 
    foreign key (SUPER_EXEC_) 
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);
    
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK 
    foreign key (TASK_ID_) 
    references ACT_RU_TASK (ID_);
    
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF(ID_);
    
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_) 
    references ACT_RU_EXECUTION (ID_);       
    
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
  add constraint ACT_FK_TASK_PROCDEF
  foreign key (PROC_DEF_ID_)
  references ACT_RE_PROCDEF (ID_);
  
alter table ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_EXE 
    foreign key (EXECUTION_ID_) 
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION(ID_);

alter table ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_BYTEARRAY 
    foreign key (BYTEARRAY_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_JOB 
    add constraint ACT_FK_JOB_EXCEPTION 
    foreign key (EXCEPTION_STACK_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION(ID_);
    
alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE 
    foreign key (EDITOR_SOURCE_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE_EXTRA 
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);
    
alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_DEPLOYMENT 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);

create table ACT_HI_PROCINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    PROC_DEF_ID_ varchar(64) not null,
    START_TIME_ datetime(3) not null,
    END_TIME_ datetime(3),
    DURATION_ bigint,
    START_USER_ID_ varchar(255),
    START_ACT_ID_ varchar(255),
    END_ACT_ID_ varchar(255),
    SUPER_PROCESS_INSTANCE_ID_ varchar(64),
    DELETE_REASON_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    primary key (ID_),
    unique (PROC_INST_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_ACTINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    ASSIGNEE_ varchar(255),
    START_TIME_ datetime(3) not null,
    END_TIME_ datetime(3),
    DURATION_ bigint,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_TASKINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_KEY_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    START_TIME_ datetime(3) not null,
    CLAIM_TIME_ datetime(3),
    END_TIME_ datetime(3),
    DURATION_ bigint,
    DELETE_REASON_ varchar(4000),
    PRIORITY_ integer,
    DUE_DATE_ datetime(3),
    FORM_KEY_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_VARINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    CREATE_TIME_ datetime(3),
    LAST_UPDATED_TIME_ datetime(3),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_DETAIL (
    ID_ varchar(64) not null,
    TYPE_ varchar(255) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(255),
    REV_ integer,
    TIME_ datetime(3) not null,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_COMMENT (
    ID_ varchar(64) not null,
    TYPE_ varchar(255),
    TIME_ datetime(3) not null,
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTION_ varchar(255),
    MESSAGE_ varchar(4000),
    FULL_MSG_ LONGBLOB,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_ATTACHMENT (
    ID_ varchar(64) not null,
    REV_ integer,
    USER_ID_ varchar(255),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(4000),
    TYPE_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    URL_ varchar(4000),
    CONTENT_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_IDENTITYLINK (
    ID_ varchar(64),
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_);
create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);
create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);
create index ACT_IDX_HI_IDENT_LNK_USER on ACT_HI_IDENTITYLINK(USER_ID_);
create index ACT_IDX_HI_IDENT_LNK_TASK on ACT_HI_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_PROCINST on ACT_HI_IDENTITYLINK(PROC_INST_ID_);

create table ACT_ID_GROUP (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    TYPE_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_ID_MEMBERSHIP (
    USER_ID_ varchar(64),
    GROUP_ID_ varchar(64),
    primary key (USER_ID_, GROUP_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_ID_USER (
    ID_ varchar(64),
    REV_ integer,
    FIRST_ varchar(255),
    LAST_ varchar(255),
    EMAIL_ varchar(255),
    PWD_ varchar(255),
    PICTURE_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_ID_INFO (
    ID_ varchar(64),
    REV_ integer,
    USER_ID_ varchar(64),
    TYPE_ varchar(64),
    KEY_ varchar(255),
    VALUE_ varchar(255),
    PASSWORD_ LONGBLOB,
    PARENT_ID_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

alter table ACT_ID_MEMBERSHIP 
    add constraint ACT_FK_MEMB_GROUP 
    foreign key (GROUP_ID_) 
    references ACT_ID_GROUP (ID_);

alter table ACT_ID_MEMBERSHIP 
    add constraint ACT_FK_MEMB_USER 
    foreign key (USER_ID_) 
    references ACT_ID_USER (ID_);

-- Quartz 2.2.1 schema
CREATE TABLE QRTZ_JOB_DETAILS(SCHED_NAME VARCHAR(120) NOT NULL,JOB_NAME VARCHAR(200) NOT NULL,JOB_GROUP VARCHAR(200) NOT NULL,DESCRIPTION VARCHAR(250) NULL,JOB_CLASS_NAME VARCHAR(250) NOT NULL,IS_DURABLE VARCHAR(1) NOT NULL,IS_NONCONCURRENT VARCHAR(1) NOT NULL,IS_UPDATE_DATA VARCHAR(1) NOT NULL,REQUESTS_RECOVERY VARCHAR(1) NOT NULL,JOB_DATA BLOB NULL,PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP))ENGINE=InnoDB;
CREATE TABLE QRTZ_TRIGGERS (SCHED_NAME VARCHAR(120) NOT NULL,TRIGGER_NAME VARCHAR(200) NOT NULL,TRIGGER_GROUP VARCHAR(200) NOT NULL,JOB_NAME VARCHAR(200) NOT NULL,JOB_GROUP VARCHAR(200) NOT NULL,DESCRIPTION VARCHAR(250) NULL,NEXT_FIRE_TIME BIGINT(13) NULL,PREV_FIRE_TIME BIGINT(13) NULL,PRIORITY INTEGER NULL,TRIGGER_STATE VARCHAR(16) NOT NULL,TRIGGER_TYPE VARCHAR(8) NOT NULL,START_TIME BIGINT(13) NOT NULL,END_TIME BIGINT(13) NULL,CALENDAR_NAME VARCHAR(200) NULL,MISFIRE_INSTR SMALLINT(2) NULL,JOB_DATA BLOB NULL,PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP))ENGINE=InnoDB;
CREATE TABLE QRTZ_SIMPLE_TRIGGERS (SCHED_NAME VARCHAR(120) NOT NULL,TRIGGER_NAME VARCHAR(200) NOT NULL,TRIGGER_GROUP VARCHAR(200) NOT NULL,REPEAT_COUNT BIGINT(7) NOT NULL,REPEAT_INTERVAL BIGINT(12) NOT NULL,TIMES_TRIGGERED BIGINT(10) NOT NULL,PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))ENGINE=InnoDB;
CREATE TABLE QRTZ_CRON_TRIGGERS (SCHED_NAME VARCHAR(120) NOT NULL,TRIGGER_NAME VARCHAR(200) NOT NULL,TRIGGER_GROUP VARCHAR(200) NOT NULL,CRON_EXPRESSION VARCHAR(120) NOT NULL,TIME_ZONE_ID VARCHAR(80),PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))ENGINE=InnoDB;
CREATE TABLE QRTZ_SIMPROP_TRIGGERS  (SCHED_NAME VARCHAR(120) NOT NULL,TRIGGER_NAME VARCHAR(200) NOT NULL,TRIGGER_GROUP VARCHAR(200) NOT NULL,STR_PROP_1 VARCHAR(512) NULL,STR_PROP_2 VARCHAR(512) NULL,STR_PROP_3 VARCHAR(512) NULL,INT_PROP_1 INT NULL,INT_PROP_2 INT NULL,LONG_PROP_1 BIGINT NULL,LONG_PROP_2 BIGINT NULL,DEC_PROP_1 NUMERIC(13,4) NULL,DEC_PROP_2 NUMERIC(13,4) NULL,BOOL_PROP_1 VARCHAR(1) NULL,BOOL_PROP_2 VARCHAR(1) NULL,PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))ENGINE=InnoDB;
CREATE TABLE QRTZ_BLOB_TRIGGERS (SCHED_NAME VARCHAR(120) NOT NULL,TRIGGER_NAME VARCHAR(200) NOT NULL,TRIGGER_GROUP VARCHAR(200) NOT NULL,BLOB_DATA BLOB NULL,PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),INDEX (SCHED_NAME,TRIGGER_NAME, TRIGGER_GROUP),FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))ENGINE=InnoDB;
CREATE TABLE QRTZ_CALENDARS (SCHED_NAME VARCHAR(120) NOT NULL,CALENDAR_NAME VARCHAR(200) NOT NULL,CALENDAR BLOB NOT NULL,PRIMARY KEY (SCHED_NAME,CALENDAR_NAME))ENGINE=InnoDB;
CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (SCHED_NAME VARCHAR(120) NOT NULL,TRIGGER_GROUP VARCHAR(200) NOT NULL,PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP))ENGINE=InnoDB;
CREATE TABLE QRTZ_FIRED_TRIGGERS (SCHED_NAME VARCHAR(120) NOT NULL,ENTRY_ID VARCHAR(95) NOT NULL,TRIGGER_NAME VARCHAR(200) NOT NULL,TRIGGER_GROUP VARCHAR(200) NOT NULL,INSTANCE_NAME VARCHAR(200) NOT NULL,FIRED_TIME BIGINT(13) NOT NULL,SCHED_TIME BIGINT(13) NOT NULL,PRIORITY INTEGER NOT NULL,STATE VARCHAR(16) NOT NULL,JOB_NAME VARCHAR(200) NULL,JOB_GROUP VARCHAR(200) NULL,IS_NONCONCURRENT VARCHAR(1) NULL,REQUESTS_RECOVERY VARCHAR(1) NULL,PRIMARY KEY (SCHED_NAME,ENTRY_ID))ENGINE=InnoDB;
CREATE TABLE QRTZ_SCHEDULER_STATE (SCHED_NAME VARCHAR(120) NOT NULL,INSTANCE_NAME VARCHAR(200) NOT NULL,LAST_CHECKIN_TIME BIGINT(13) NOT NULL,CHECKIN_INTERVAL BIGINT(13) NOT NULL,PRIMARY KEY (SCHED_NAME,INSTANCE_NAME))ENGINE=InnoDB;
CREATE TABLE QRTZ_LOCKS (SCHED_NAME VARCHAR(120) NOT NULL,LOCK_NAME VARCHAR(40) NOT NULL,PRIMARY KEY (SCHED_NAME,LOCK_NAME))ENGINE=InnoDB;
CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);

-- minimal init data

-- CM_BASE_CATELOG: 2 rows
INSERT INTO `CM_BASE_CATELOG` (`ID`, `BUSI_NAME`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('SYS', '系统字典', '0', '2013-11-10 16:43:40', '2013-11-10 15:18:48', '系统字典');
INSERT INTO `CM_BASE_CATELOG` (`ID`, `BUSI_NAME`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('BUSI', '业务通用', '1', '2013-11-10 16:43:40', '2013-11-10 15:18:48', '业务通用');

-- CM_BASE_DATA: 32 rows
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'black', NULL, '黑色', '0', NULL, '2013-09-24 01:01:13', '2013-09-24 01:01:13', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'blue', NULL, '蓝色', '2', NULL, '2013-07-13 15:07:22', '2013-07-13 15:07:22', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'gray', NULL, '灰色', '4', NULL, '2013-07-13 15:07:55', '2013-07-13 15:07:55', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'green', NULL, '绿色', '3', NULL, '2013-07-13 15:07:44', '2013-07-13 15:07:44', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'other', NULL, '其他', '99', NULL, '2013-09-24 01:02:51', '2013-09-24 01:01:49', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'red', NULL, '红色', '1', NULL, '2013-07-13 15:06:49', '2013-07-13 15:06:49', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'silver', NULL, '银色', '6', NULL, '2013-09-24 01:01:35', '2013-09-24 01:01:35', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('COLOR', 'yellow', NULL, '黄色', '5', NULL, '2013-07-13 15:08:04', '2013-07-13 15:08:04', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('FILE_PIXEL', 'csv', NULL, 'Excel(不带格式)', '6', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('FILE_PIXEL', 'pdf', NULL, 'PDF文件', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('FILE_PIXEL', 'xls', NULL, 'Excel文件', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('FONT-WEIGHT', 'bold', NULL, '粗体', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('FONT-WEIGHT', 'normal', NULL, '普通', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('HAVE_NOT', '0', NULL, '无', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('HAVE_NOT', '1', NULL, '有', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('OPR_MODE', '1', NULL, '新增', '0', NULL, '2013-07-17 19:16:50', '2013-07-17 19:16:50', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('OPR_MODE', '2', NULL, '修改', '1', NULL, '2013-07-17 19:16:58', '2013-07-17 19:16:58', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('OPR_MODE', '3', NULL, '删除', '2', NULL, '2013-07-17 19:17:07', '2013-07-17 19:17:07', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('SEX', '0', NULL, '未知', '0', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('SEX', '1', NULL, '男', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('SEX', '2', NULL, '女', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('SORT_DIR', 'asc', NULL, '升序', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('SORT_DIR', 'desc', NULL, '降序', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('STYLE_CLASS', 'center', NULL, '居中', '3', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('STYLE_CLASS', 'left', NULL, '左侧', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('STYLE_CLASS', 'right', NULL, '右侧', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('YES_NO', '0', NULL, '否', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('YES_NO', '1', NULL, '是', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('OPEN_CLOSE', '0', NULL, '关', '2', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('OPEN_CLOSE', '1', NULL, '开', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('THEME_BACKGROUD', '0', NULL, '浅色', '0', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);
INSERT INTO `CM_BASE_DATA` (`DATA_TYPE`, `DATA_CODE`, `PARENT_CODE`, `SHOW_NAME`, `SORT`, `EXTRA`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`) VALUES ('THEME_BACKGROUD', '1', NULL, '深色', '1', NULL, '2013-06-02 21:26:01', '2013-06-02 21:26:01', NULL);

-- CM_BASE_TYPE: 11 rows
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('COLOR', '颜色', '2013-07-13 15:06:24', '2013-07-13 15:06:07', NULL, 'BUSI', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('FILE_PIXEL', '文件后缀', '2013-06-06 22:55:46', '2013-06-02 21:22:21', NULL, 'SYS', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('FONT-WEIGHT', '字体样式', '2013-06-06 22:55:52', '2013-06-02 21:22:21', NULL, 'SYS', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('HAVE_NOT', '有无', '2013-06-06 22:55:57', '2013-06-02 21:22:21', NULL, 'SYS', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('OPR_MODE', '操作类型', '2013-07-17 19:16:40', '2013-07-17 19:16:40', '日志操作类型', 'BUSI', '3');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('SEX', '性别', '2013-06-06 22:56:53', '2013-06-02 21:22:21', NULL, 'BUSI', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('SORT_DIR', '排序方向', '2013-06-06 22:56:06', '2013-06-02 21:22:21', NULL, 'SYS', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('STYLE_CLASS', '样式', '2013-06-06 22:56:18', '2013-06-02 21:22:21', NULL, 'SYS', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('OPEN_CLOSE', '开关', '2013-06-06 22:56:27', '2013-06-02 21:22:21', NULL, 'SYS', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('YES_NO', '是否', '2013-06-06 22:56:27', '2013-06-02 21:22:21', NULL, 'SYS', '1');
INSERT INTO `CM_BASE_TYPE` (`DATA_TYPE`, `BUSI_NAME`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `CATELOG`, `SORT`) VALUES ('THEME_BACKGROUD', '底色风格', '2013-06-06 22:56:27', '2013-06-02 21:22:21', NULL, 'SYS', '1');

-- CM_DOMAIN: 1 rows
INSERT INTO `CM_DOMAIN` (`DOMAIN_KEY`, `SYS_FLAG`, `BUSI_NAME`, `SORT`, `ICON`, `UPDATE_DATE`, `CREATE_DATE`, `DESCRIPTION`, `PRI`, `COLUMNS`) VALUES ('manage', '1', '功能设置', '0', 'wrench', '2013-12-23 17:42:19', '2013-06-02 00:00:00', NULL, 'manage', '50;50');

-- CM_MENU: 27 rows
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('wx', '1', NULL, NULL, 'manage', 'weixin.png', '微信管理', '1', '1', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'wx');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('wx_mp', '1', 'wx', '/wx/MpConfigAction/index.shtml', 'manage', 'weixin_1.png', '公众号开发', '1', '2', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'wx_mp');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('wx_agent', '1', 'wx', '/wx/AgentConfigAction/index.shtml', 'manage', 'weixin_qy.png', '企业号开发', '1', '3', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'wx_agent');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('wx_xcx', '1', 'wx', NULL, 'manage', 'rgb.png', '小程序开发', '1', '4', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'wx_xcx');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('wx_command', '1', 'wx', '/wx/CommandConfigAction/index.shtml', 'manage', 'script_code.png', '事件处理器', '1', '5', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'wx_command');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('wx_url', '1', 'wx', '/wx/UrlConfigAction/index.shtml', 'manage', 'link_edit.png', '外部超链接', '1', '6', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'wx_url');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('user', '1', NULL, NULL, 'manage', 'user.png', '用户权限', '1', '7', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'user');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('user_group', '1', 'user', '/manager/user/UserGroupAction/index.shtml', 'manage', 'user_home.png', '组织架构', '1', '8', '2014-11-05 23:18:52', '2013-09-28 17:27:26', '2', NULL, 'user_group');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('user_pri', '1', 'user', '/manager/pri/PriGroupAction/index.shtml', 'manage', 'user_key.png', '用户权限', '1', '9', '2014-11-05 23:18:52', '2013-09-28 17:30:09', '2', NULL, 'user_pri');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys', '1', NULL, NULL, 'manage', 'cog.png', '系统开发', '1', '10', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_table', '1', 'sys', '/development/table/TableAction/index.shtml', 'manage', 'table_gear.png', '数据库开发', '1', '11', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_table');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_menu', '1', 'sys', '/manager/MenuAction/index.shtml', 'manage', 'application_side_list.png', '用户菜单', '1', '12', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_menu');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_thirdpart', '1', 'sys', '/thirdpart/ThirdpartAction/index.shtml', 'manage', 'link_edit.png', '第三方系统', '1', '13', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_thirdpart');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('dev_control', '1', 'sys', '/development/ControlAction/index.shtml', 'manage', 'application_xp_terminal.png', '调试控制台', '1', '14', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'dev_control');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_function', '1', 'sys', '/development/FunctionAction/index.shtml', 'manage', 'page_white_code.png', '系统函数', '1', '15', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_function');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_schedule', '1', 'sys', '/manager/job/JobAction/index.shtml', 'manage', 'calendar.png', '计划任务', '1', '16', '2014-11-05 23:18:52', '2013-09-28 17:30:09', '2', NULL, 'sys_schedule');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_queue', '1', 'sys', '/manager/queue/QueueAction/index.shtml', 'manage', 'calendar.png', '异步队列', '1', '17', '2014-11-05 23:18:52', '2013-09-28 17:30:09', '2', NULL, 'sys_queue');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_dict', '1', 'sys', '/manager/db/DbAction/index.shtml', 'manage', 'book.png', '数据字典', '1', '18', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_dict');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_template', '1', 'sys', '/development/TemplateAction/index.shtml', 'manage', 'page_save.png', '快照发布', '1', '19', '2014-11-05 23:18:52', '2013-09-28 17:30:09', '2', NULL, 'sys_template');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_module', '1', 'sys', NULL, 'manage', NULL, '模块开发', '1', '20', '2014-11-05 23:18:52', '2013-09-28 17:30:09', '2', NULL, 'sys_module');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_view', '1', 'sys_module', '/development/view/ViewConfigAction/index.shtml', 'manage', 'application_view_tile.png', '视图模块', '1', '21', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_view');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_combo', '1', 'sys_module', '/development/widget/WidgetConfigAction/index.shtml', 'manage', 'date.png', '数据控件', '1', '22', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_combo');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_widget', '1', 'sys_module', '/development/widget/TemplateWidgetAction/index.shtml', 'manage', 'application_form.png', '模板控件', '1', '23', '2014-11-05 23:18:52', '2013-06-09 00:16:07', '2', NULL, 'sys_widget');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('flow', '1', 'sys', NULL, 'manage', NULL, '工作流开发', '1', '24', '2014-11-05 23:18:52', '2013-10-08 10:44:57', '2', NULL, 'flow');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('flow_model', '1', 'flow', '/flow/ModelAction/index.shtml', 'manage', 'group_edit.png', '流程图设计', '1', '25', '2014-11-05 23:18:52', '2013-10-08 11:00:03', '2', NULL, 'flow_model');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('flow_pd', '1', 'flow', '/flow/PdAction/index.shtml', 'manage', 'group_gear.png', '流程设置', '1', '26', '2014-11-05 23:18:52', '2013-10-08 11:04:29', '2', NULL, 'flow_pd');
INSERT INTO `CM_MENU` (`ID`, `SYS_FLAG`, `PARENT_ID`, `ACTION`, `DOMAIN_KEY`, `ICON`, `NAME`, `OPEN_TYPE`, `SORT`, `UPDATE_DATE`, `CREATE_DATE`, `PARAM_TYPE`, `PARAM_SCRIPT`, `PRI`) VALUES ('sys_panel', '1', NULL, '/development/SystemAction/index.shtml', 'manage', 'server_wrench.png', '控制面板', '1', '27', '2014-11-05 23:27:41', '2014-11-05 22:39:41', '2', NULL, 'sys_panel');

-- CM_PRI: 28 rows
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('manage', '1', 'manage', '功能设置', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('wx', '1', 'wx', '微信管理', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('wx_mp', '1', 'wx_mp', '公众号开发', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('wx_agent', '1', 'wx_agent', '企业号开发', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('wx_xcx', '1', 'wx_xcx', '小程序开发', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('wx_command', '1', 'wx_command', '事件处理器', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('wx_url', '1', 'wx_url', '外部超链接', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('user', '1', 'user', '用户权限', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('user_group', '1', 'user_group', '组织架构', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('user_pri', '1', 'user_pri', '用户权限', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys', '1', 'sys', '系统开发', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_table', '1', 'sys_table', '数据库开发', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_menu', '1', 'sys_menu', '用户菜单', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_thirdpart', '1', 'sys_thirdpart', '第三方系统', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('dev_control', '1', 'dev_control', '调试控制台', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_function', '1', 'sys_function', '系统函数', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_schedule', '1', 'sys_schedule', '计划任务', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_queue', '1', 'sys_queue', '异步队列', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_dict', '1', 'sys_dict', '数据字典', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_template', '1', 'sys_template', '快照发布', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_module', '1', 'sys_module', '模块开发', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_view', '1', 'sys_view', '视图模块', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_combo', '1', 'sys_combo', '数据控件', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_widget', '1', 'sys_widget', '模板控件', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('flow', '1', 'flow', '工作流开发', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('flow_model', '1', 'flow_model', '流程图设计', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('flow_pd', '1', 'flow_pd', '流程设置', NULL, '1', '2', '${true}');
INSERT INTO `CM_PRI` (`PRI_KEY`, `CATELOG_TYPE`, `CATELOG_KEY`, `BUSI_NAME`, `DESCRIPTION`, `TYPE`, `CHECK_TYPE`, `CHECK_SCRIPT`) VALUES ('sys_panel', '1', 'sys_panel', '控制面板', NULL, '1', '2', '${true}');

-- US_USER: 1 rows
INSERT INTO `US_USER` (`USER_ID`, `PASSWORD`, `BUSI_NAME`, `SYS_FLAG`, `ACTIVE_FLAG`, `SELECT_FLAG`, `EFF_DATE`, `END_DATE`, `UPDATE_DATE`, `CREATE_DATE`, `ALLOW_IP`, `SORT`, `MAIL`, `MSG_TYPE`, `RECEIVE_TYPE`, `MOBILE`, `WXID`, `WX_ENABLE`, `WX_STATUS`, `WX_AVATAR`) VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', '管理员', '1', '1', '1', '1900-01-01 00:00:00', '2099-01-01 00:00:00', '2013-09-02 23:50:12', '1900-01-01 00:00:00', NULL, '0', NULL, NULL, NULL, NULL, NULL, '0', '0', NULL);

-- US_GROUP: 2 rows
INSERT INTO `US_GROUP` (`GROUP_KEY`, `PARENT_KEY`, `BUSI_NAME`, `SYS_FLAG`, `UPDATE_DATE`, `CREATE_DATE`, `SORT`, `WX_DEPARTMENT_ID`) VALUES ('admin', NULL, '管理组', '1', '1900-01-01 00:00:00', '1900-01-01 00:00:00', '0', NULL);
INSERT INTO `US_GROUP` (`GROUP_KEY`, `PARENT_KEY`, `BUSI_NAME`, `SYS_FLAG`, `UPDATE_DATE`, `CREATE_DATE`, `SORT`, `WX_DEPARTMENT_ID`) VALUES ('visitor', NULL, '访客组', '1', '1900-01-01 00:00:00', '1900-01-01 00:00:00', '1', NULL);

-- US_ROLE: 2 rows
INSERT INTO `US_ROLE` (`ROLE_KEY`, `BUSI_NAME`, `SYS_FLAG`, `UPDATE_DATE`, `CREATE_DATE`, `SORT`) VALUES ('admin', '管理员', '1', '2013-06-02 20:09:47', '1900-01-01 00:00:00', '0');
INSERT INTO `US_ROLE` (`ROLE_KEY`, `BUSI_NAME`, `SYS_FLAG`, `UPDATE_DATE`, `CREATE_DATE`, `SORT`) VALUES ('visitor', '访客', '1', '2013-06-02 20:09:47', '1900-01-01 00:00:00', '1');

-- US_GROUP_ROLE: 2 rows
INSERT INTO `US_GROUP_ROLE` (`GROUP_KEY`, `ROLE_KEY`, `SYS_FLAG`, `CREATE_DATE`, `UPDATE_DATE`) VALUES ('admin', 'admin', '1', '1900-01-01 00:00:00', '1900-01-01 00:00:00');
INSERT INTO `US_GROUP_ROLE` (`GROUP_KEY`, `ROLE_KEY`, `SYS_FLAG`, `CREATE_DATE`, `UPDATE_DATE`) VALUES ('visitor', 'visitor', '1', '1900-01-01 00:00:00', '1900-01-01 00:00:00');

-- US_USER_GROUP_ROLE: 1 rows
INSERT INTO `US_USER_GROUP_ROLE` (`USER_ID`, `GROUP_KEY`, `ROLE_KEY`, `SYS_FLAG`, `CREATE_DATE`, `UPDATE_DATE`, `DEFAULT_FLAG`, `SORT`) VALUES ('admin', 'admin', 'admin', '1', '2013-06-02 20:07:47', '2013-06-02 20:07:47', '1', '0');

SET FOREIGN_KEY_CHECKS=1;

-- bpmt-lite v1.5.0 OAuth login schema
-- This fragment is appended to both full and minimal initialization SQL files.

CREATE TABLE IF NOT EXISTS `CM_THIRDPART` (
  `THIRDPART_KEY` varchar(100) NOT NULL COMMENT '外部系统业务主键',
  `THIRDPART_NAME` varchar(200) NOT NULL COMMENT '外部系统名称',
  `CLIENT_ID` varchar(100) NOT NULL COMMENT 'OAuth Client ID',
  `CLIENT_SECRET_HASH` varchar(128) NOT NULL COMMENT 'OAuth Client Secret Hash',
  `REDIRECT_URIS` longtext NOT NULL COMMENT 'OAuth 回调地址白名单',
  `HOME_URL` varchar(500) DEFAULT NULL COMMENT '第三方系统入口地址',
  `PRI_KEY` varchar(100) NOT NULL COMMENT '外部系统权限点',
  `ACTIVE_FLAG` int(11) NOT NULL DEFAULT '1' COMMENT '启用状态',
  `DESCRIPTION` varchar(500) DEFAULT NULL COMMENT '说明',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `UPDATE_TIME` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`THIRDPART_KEY`),
  UNIQUE KEY `UK_CM_THIRDPART_CLIENT_ID` (`CLIENT_ID`),
  KEY `IDX_CM_THIRDPART_PRI_KEY` (`PRI_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='外部系统主数据';

CREATE TABLE IF NOT EXISTS `CM_THIRDPART_AUTH_CODE` (
  `ID` varchar(100) NOT NULL COMMENT 'ID',
  `CODE_HASH` varchar(128) NOT NULL COMMENT '授权码 Hash',
  `CLIENT_ID` varchar(100) NOT NULL COMMENT 'OAuth Client ID',
  `THIRDPART_KEY` varchar(100) NOT NULL COMMENT '外部系统业务主键',
  `USER_ID` varchar(100) NOT NULL COMMENT '用户 ID',
  `REDIRECT_URI` varchar(500) NOT NULL COMMENT 'OAuth 回调地址',
  `STATE` varchar(500) DEFAULT NULL COMMENT 'OAuth state',
  `EXPIRES_AT` datetime NOT NULL COMMENT '过期时间',
  `USED_AT` datetime DEFAULT NULL COMMENT '使用时间',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_CM_THIRDPART_AUTH_CODE_HASH` (`CODE_HASH`),
  KEY `IDX_CM_THIRDPART_AUTH_CODE_CLIENT_ID` (`CLIENT_ID`),
  KEY `IDX_CM_THIRDPART_AUTH_CODE_USER_ID` (`USER_ID`),
  KEY `IDX_CM_THIRDPART_AUTH_CODE_EXPIRES_AT` (`EXPIRES_AT`),
  KEY `IDX_CM_THIRDPART_AUTH_CODE_USED_AT` (`USED_AT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='外部系统 OAuth 授权码运行数据';

CREATE TABLE IF NOT EXISTS `CM_THIRDPART_ACCESS_TOKEN` (
  `ID` varchar(100) NOT NULL COMMENT 'ID',
  `TOKEN_HASH` varchar(128) NOT NULL COMMENT 'Access Token Hash',
  `CLIENT_ID` varchar(100) NOT NULL COMMENT 'OAuth Client ID',
  `THIRDPART_KEY` varchar(100) NOT NULL COMMENT '外部系统业务主键',
  `USER_ID` varchar(100) NOT NULL COMMENT '用户 ID',
  `EXPIRES_AT` datetime NOT NULL COMMENT '过期时间',
  `REVOKED_AT` datetime DEFAULT NULL COMMENT '吊销时间',
  `LAST_USED_AT` datetime DEFAULT NULL COMMENT '最后使用时间',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_CM_THIRDPART_ACCESS_TOKEN_HASH` (`TOKEN_HASH`),
  KEY `IDX_CM_THIRDPART_ACCESS_TOKEN_CLIENT_ID` (`CLIENT_ID`),
  KEY `IDX_CM_THIRDPART_ACCESS_TOKEN_USER_ID` (`USER_ID`),
  KEY `IDX_CM_THIRDPART_ACCESS_TOKEN_EXPIRES_AT` (`EXPIRES_AT`),
  KEY `IDX_CM_THIRDPART_ACCESS_TOKEN_REVOKED_AT` (`REVOKED_AT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='外部系统 OAuth Access Token 运行数据';
