-- APP版本管理模块数据库表结构

CREATE TABLE IF NOT EXISTS `sys_app_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version_name` VARCHAR(64) NOT NULL COMMENT '版本名称(如v2.0.0)',
  `version_code` VARCHAR(32) NOT NULL COMMENT '版本号(如2.0.0)',
  `platform` VARCHAR(20) NOT NULL DEFAULT 'android' COMMENT '平台(android/ios)',
  `update_url` VARCHAR(500) DEFAULT NULL COMMENT '更新下载地址',
  `update_content` TEXT COMMENT '更新内容(JSON数组)',
  `is_force_update` TINYINT DEFAULT 0 COMMENT '是否强制更新(0:否 1:是)',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用(0:否 1:是)',
  -- 生成列：同平台唯一生效版本约束
  -- 启用时值为 'platform:enabled'（如 android:enabled），唯一约束生效
  -- 禁用时值为 NULL，多个 NULL 不受唯一约束限制
  `enabled_guard` VARCHAR(30) GENERATED ALWAYS AS (
    IF(`is_enabled` = 1, CONCAT(`platform`, ':enabled'), NULL)
  ) STORED,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_platform` (`platform`),
  KEY `idx_version_code` (`version_code`),
  UNIQUE KEY `uk_platform_enabled` (`enabled_guard`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP版本管理表';

-- 菜单和权限数据（请根据实际 parent_id 调整）
--
-- 说明：
-- 1. 查找系统管理菜单ID: SELECT id FROM sys_menu WHERE name = '系统管理';
-- 2. 将下面 SQL 中的 @parent_id 替换为实际值
-- 3. 执行 INSERT 语句
--
-- 菜单记录：APP版本管理
-- INSERT INTO sys_menu (parent_id, name, type, system_type, path, component, perm, visible, sort, icon, keep_alive, always_show, create_time, update_time)
-- VALUES (@parent_id, 'APP版本管理', '1', 'admin', 'appVersion', 'system/appVersion/index', NULL, 1, 99, 'iphone', 0, 0, NOW(), NOW());
--
-- 按钮权限：新增
-- INSERT INTO sys_menu (parent_id, name, type, system_type, perm, visible, sort, create_time, update_time)
-- VALUES (@menu_id, '新增APP版本', '4', 'admin', 'sys:app_version:add', 1, 1, NOW(), NOW());
--
-- 按钮权限：编辑
-- INSERT INTO sys_menu (parent_id, name, type, system_type, perm, visible, sort, create_time, update_time)
-- VALUES (@menu_id, '修改APP版本', '4', 'admin', 'sys:app_version:edit', 1, 2, NOW(), NOW());
--
-- 按钮权限：删除
-- INSERT INTO sys_menu (parent_id, name, type, system_type, perm, visible, sort, create_time, update_time)
-- VALUES (@menu_id, '删除APP版本', '4', 'admin', 'sys:app_version:delete', 1, 3, NOW(), NOW());
