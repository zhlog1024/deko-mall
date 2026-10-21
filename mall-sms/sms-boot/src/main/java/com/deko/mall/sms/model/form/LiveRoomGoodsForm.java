package com.deko.mall.sms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 直播商品挂载表单（管理端）
 */
@Data
public class LiveRoomGoodsForm {

    @Schema(description = "直播间ID")
    private Long roomId;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "排序(越小越靠前)")
    private Integer sort;
}
