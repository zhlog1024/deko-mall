package com.deko.mall.sms.converter;

import com.deko.mall.sms.model.entity.SmsLiveRoom;
import com.deko.mall.sms.model.entity.SmsLiveRoomGoods;
import com.deko.mall.sms.model.vo.LiveGoodsVO;
import com.deko.mall.sms.model.vo.LiveRoomListVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MapStruct 转换器字段映射测试
 * <p>
 * 重点守护「源/目标字段名不同」的映射：这类映射漏写时 MapStruct 不会报错，
 * 只会在运行期静默留空（曾导致商品只有图、无标题无价格）。
 */
class LiveConverterTest {

    private final LiveConverter converter = new LiveConverterImpl();

    @Test
    @DisplayName("商品转换：goodsTitle→title、goodsPrice→price、goodsImage→images、isExplain→liveGoodsIsExplain")
    void goods2Vo_shouldMapRenamedFields() {
        SmsLiveRoomGoods entity = new SmsLiveRoomGoods();
        entity.setId(2L);
        entity.setRoomId(1L);
        entity.setSpuId(961L);
        entity.setGoodsTitle("有机甜菜根粉45g");
        entity.setGoodsImage("https://cdn.example.com/a.jpg");
        entity.setGoodsPrice(new BigDecimal("10.90"));
        entity.setInventory(700);
        entity.setIsExplain(1);

        LiveGoodsVO vo = converter.goods2Vo(entity);

        assertNotNull(vo);
        assertEquals(2L, vo.getId());
        assertEquals(961L, vo.getSpuId());
        assertEquals("有机甜菜根粉45g", vo.getTitle(), "goodsTitle 必须映射到 title");
        assertEquals(0, new BigDecimal("10.90").compareTo(vo.getPrice()), "goodsPrice 必须映射到 price");
        assertEquals("https://cdn.example.com/a.jpg", vo.getImages(), "goodsImage 必须映射到 images");
        assertEquals(700, vo.getInventory());
        assertEquals(1, vo.getLiveGoodsIsExplain(), "isExplain 必须映射到 liveGoodsIsExplain");
    }

    @Test
    @DisplayName("列表转换：anchorName→sellerName（前端 item.seller_name 依赖）")
    void entity2ListVo_shouldMapAnchorNameToSellerName() {
        SmsLiveRoom room = new SmsLiveRoom();
        room.setId(1L);
        room.setTitle("测试直播间");
        room.setAnchorName("主播小张");

        LiveRoomListVO vo = converter.entity2ListVo(room);

        assertNotNull(vo);
        assertEquals("主播小张", vo.getSellerName(), "anchorName 必须映射到 sellerName");
        assertEquals("测试直播间", vo.getTitle());
    }
}
