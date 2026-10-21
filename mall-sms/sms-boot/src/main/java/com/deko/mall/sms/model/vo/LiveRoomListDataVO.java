package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * 直播间列表数据（前端契约：data{data_list:[...]}）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveRoomListDataVO {

    /**
     * 直播间列表（index 页读取 data_list）
     */
    private List<LiveRoomListVO> dataList;

    /**
     * 直播间列表（search 页读取 data）
     */
    private List<LiveRoomListVO> data;

    /**
     * 总页数（search 页读取 page_total）
     */
    private Long pageTotal;

    /**
     * 总条数（search 页读取 total）
     */
    private Long total;
}
