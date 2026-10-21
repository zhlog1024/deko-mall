package com.deko.mall.ums.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

@Data
public class UmsGoodsBrowse extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Long spuId;

    private Integer deleted;
}
