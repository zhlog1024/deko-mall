-- 附件管理模块数据库表结构
--
-- 说明：附件管理功能在 mall-system 模块中实现
--
-- 附件分类表
CREATE TABLE IF NOT EXISTS `sys_attachment_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID（0表示根分类）',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用（1:启用;0:禁用）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件分类表';

-- 附件信息表
CREATE TABLE IF NOT EXISTS `sys_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(255) NOT NULL COMMENT '附件名称',
  `original_name` VARCHAR(255) COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径（OSS路径）',
  `file_url` VARCHAR(500) COMMENT '文件访问URL',
  `file_type` VARCHAR(50) COMMENT '文件类型（image/video/file）',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `file_ext` VARCHAR(20) COMMENT '文件扩展名',
  `category_id` BIGINT COMMENT '分类ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件信息表';

