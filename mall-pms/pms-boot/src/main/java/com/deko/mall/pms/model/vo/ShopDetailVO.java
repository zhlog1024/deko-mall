package com.deko.mall.pms.model.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺详情视图对象
 */
@Data
@Schema(description = "店铺详情视图对象")
public class ShopDetailVO {

    @Schema(description = "店铺信息")
    private ShopVO shop;

    @Schema(description = "店铺商品分页列表")
    private IPage<SpuPageVO> goodsPage;
}
