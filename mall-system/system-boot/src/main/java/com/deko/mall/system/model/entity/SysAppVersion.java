package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * APP版本管理实体
 */
@Data
@TableName("sys_app_version")
public class SysAppVersion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 版本号
     */
    private String versionCode;

    /**
     * 平台(android/ios)
     */
    private String platform;

    /**
     * 更新下载地址
     */
    private String updateUrl;

    /**
     * 更新内容(JSON数组)
     */
    private String updateContent;

    /**
     * 是否强制更新(0:否;1:是)
     */
    private Integer isForceUpdate;

    /**
     * 是否启用(0:否;1:是)
     */
    private Integer isEnabled;

    /**
     * 生成列(MySQL管理，禁止插入/更新)
     */
    @TableField(insertStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.NEVER,
                updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.NEVER)
    private String enabledGuard;
}
