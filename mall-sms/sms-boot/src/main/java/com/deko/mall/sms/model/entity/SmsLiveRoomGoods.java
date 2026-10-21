package com.deko.mall.sms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 直播商品挂载表
 */
@TableName(value = "sms_live_room_goods")
@Data
public class SmsLiveRoomGoods extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 直播间ID
     */
    private Long roomId;

    /**
     * 商品SPU ID
     */
    private Long spuId;

    /**
     * 商品标题(挂载时快照)
     */
    private String goodsTitle;

    /**
     * 商品主图(挂载时快照)
     */
    private String goodsImage;

    /**
     * 商品价格(元,挂载时快照)
     */
    private BigDecimal goodsPrice;

    /**
     * 库存(挂载时快照)
     */
    private Integer inventory;

    /**
     * 是否讲解中(0-否;1-是)
     */
    private Integer isExplain;

    /**
     * 排序(越小越靠前)
     */
    private Integer sort;

    /**
     * 逻辑删除标识(0-正常;1-删除)
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
