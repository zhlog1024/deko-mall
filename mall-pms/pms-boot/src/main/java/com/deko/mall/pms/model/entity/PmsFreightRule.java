package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 运费规则实体
 */
@Data
@TableName("pms_freight_rule")
@EqualsAndHashCode(callSuper = true)
public class PmsFreightRule extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 运费模板ID
     */
    private Long templateId;

    /**
     * 区域编码集合,多个以逗号分隔
     */
    private String regionCodes;

    /**
     * 区域名称集合,多个以逗号分隔
     */
    private String regionNames;

    /**
     * 首件数量/首重数量
     */
    private Integer firstItem;

    /**
     * 首费(单位：元)
     */
    private BigDecimal firstFee;

    /**
     * 续件数量/续重费用
     */
    private Integer continueItem;

    /**
     * 续费(单位：元)
     */
    private BigDecimal continueFee;

    /**
     * 是否为默认规则(0-否,1-是)
     */
    private Integer isDefault;

    /**
     * 删除标志(0-未删除,1-已删除)
     */
    @TableLogic
    private Integer deleted;


}
