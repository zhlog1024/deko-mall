package com.deko.mall.ums.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员统一收藏实体
 */
@Data
@TableName("ums_favor")
@EqualsAndHashCode(callSuper = true)
public class UmsFavor extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    /**
     * 对象类型(GOODS-商品, SHOP-店铺)
     */
    private String targetType;

    private Long targetId;

    private Integer deleted;
}
