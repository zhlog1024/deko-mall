package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * APP全局配置实体
 */
@Data
@TableName("sys_app_config")
public class SysAppConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键 */
    private String configKey;

    /** 配置名称 */
    private String configName;

    /** 配置值 */
    private String configValue;

    /** 值类型: string/number/boolean/json/image/text */
    private String valueType;

    /** 分组: base/theme/function/customer_service/category/user/share/page_layout */
    private String groupCode;

    /** 备注 */
    private String remark;

    /** 排序 */
    private Integer sort;

    /** 状态: 1-启用, 0-禁用 */
    private Integer status;
}
