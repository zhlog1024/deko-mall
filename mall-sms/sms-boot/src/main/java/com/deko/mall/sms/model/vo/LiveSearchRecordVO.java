package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * 直播搜索记录页数据（前端契约：searchrecord 返回的 data）
 * <p>
 * 一期返回空热门搜索，前端展示本地搜索历史
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveSearchRecordVO {

    /**
     * 热门搜索 Tabs 数据（[{name, field, data:[{url,...}]}]，一期为空）
     */
    private List<Object> searchHistoryData;
}
