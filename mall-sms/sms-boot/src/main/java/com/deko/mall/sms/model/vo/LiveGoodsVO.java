package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 直播商品橱窗项（前端契约：goodsdata 列表元素）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveGoodsVO {

    /**
     * 挂载ID（直播商品ID）
     */
    private Long id;

    /**
     * 商品SPU ID（用于生成商品详情跳转地址）
     */
    private Long spuId;

    /**
     * 商品主图
     */
    private String images;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品价格(元)
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer inventory;

    /**
     * 商品详情页跳转地址
     */
    private String goodsUrl;

    /**
     * 是否讲解中(0-否;1-是)
     */
    private Integer liveGoodsIsExplain;
}
