package com.deko.mall.sms.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.sms.model.entity.SmsLiveRoom;
import com.deko.mall.sms.model.entity.SmsLiveRoomGoods;
import com.deko.mall.sms.model.vo.LiveGoodsVO;
import com.deko.mall.sms.model.vo.LiveRoomInfoVO;
import com.deko.mall.sms.model.vo.LiveRoomListVO;
import com.deko.mall.sms.model.vo.LiveRoomPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 直播实体转换器
 */
@Mapper(componentModel = "spring")
public interface LiveConverter {

    LiveRoomPageVO entity2PageVo(SmsLiveRoom entity);

    Page<LiveRoomPageVO> entity2PageVo(Page<SmsLiveRoom> po);

    @Mappings({
            @Mapping(target = "sellerName", source = "anchorName"),
            @Mapping(target = "statusName", ignore = true),
            @Mapping(target = "url", ignore = true)
    })
    LiveRoomListVO entity2ListVo(SmsLiveRoom entity);

    List<LiveRoomListVO> entity2ListVo(List<SmsLiveRoom> entities);

    LiveRoomInfoVO entity2InfoVo(SmsLiveRoom entity);

    @Mappings({
            // 以下四组源/目标字段名不同，必须显式映射，否则 MapStruct 会静默留空：
            //   goodsTitle -> title、goodsPrice -> price（漏了会导致商品只有图、无标题无价格）
            @Mapping(target = "title", source = "goodsTitle"),
            @Mapping(target = "price", source = "goodsPrice"),
            @Mapping(target = "images", source = "goodsImage"),
            @Mapping(target = "goodsUrl", ignore = true),
            @Mapping(target = "liveGoodsIsExplain", source = "isExplain")
    })
    LiveGoodsVO goods2Vo(SmsLiveRoomGoods entity);
    List<LiveGoodsVO> goods2Vo(List<SmsLiveRoomGoods> entities);
}
