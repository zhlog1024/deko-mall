package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运费模板实体
 */
@Data
@TableName("pms_freight_template")
@EqualsAndHashCode(callSuper = true)
public class PmsFreightTemplate extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 国家区域ID(sys_region.level=0)
     */
    private Long countryId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 计费方式(0-按件数,1-按重量)
     */
    private String chargeType;

    /**
     * 是否包邮(0-否,1-是)
     */
    private Integer isFreeShipping;

    /**
     * 是否默认(0-否,1-是)
     */
    private Integer isDefault;

    /**
     * 删除标志(0-未删除,1-已删除)
     */
    @TableLogic
    private Integer deleted;
}
