package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播搜索「排序依据」项
 * <p>
 * 前端契约（pages/plugins/live/search/search.vue）：
 * {@code search_order_by_list.map(o => ({name: o.name, type: o.value}))}，
 * 并以 {@code o.checked} 决定默认选中项，选中值作为接口 by 参数回传。
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveOrderByVO {

    /**
     * 显示名称（如「人气」）
     */
    private String name;

    /**
     * 排序值，作为接口 by 参数（default / hot / new）
     */
    private String value;

    /**
     * 是否默认选中
     */
    private Boolean checked;
}
