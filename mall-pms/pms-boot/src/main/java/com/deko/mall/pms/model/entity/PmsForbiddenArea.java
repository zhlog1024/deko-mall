package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 禁售区域实体
 */
@Data
@TableName("pms_forbidden_area")
@EqualsAndHashCode(callSuper = true)
public class PmsForbiddenArea extends BaseEntity {

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
     * 区域编码集合,多个以逗号分隔
     */
    private String regionCodes;

    /**
     * 区域名称集合,多个以逗号分隔
     */
    private String regionNames;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志(0-未删除,1-已删除)
     */
    @TableLogic
    private Integer deleted;
}
