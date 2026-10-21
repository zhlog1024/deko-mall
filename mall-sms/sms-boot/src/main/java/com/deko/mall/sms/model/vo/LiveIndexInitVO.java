package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * 直播首页/搜索初始化数据（前端契约：indexinit / searchinit）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveIndexInitVO {

    /**
     * 状态 Tab 列表
     */
    private List<LiveStatusTabVO> liveStatusTabList;

    /**
     * 轮播图（取已启用的广告位，字段对齐 components/slider 组件契约）
     */
    private List<LiveSliderVO> sliderList;

    /**
     * 搜索排序依据（前端契约：search_order_by_list[{name,value,checked}]）
     */
    private List<LiveOrderByVO> searchOrderByList;

    /**
     * 基础信息
     */
    private LiveBaseVO base;
}
