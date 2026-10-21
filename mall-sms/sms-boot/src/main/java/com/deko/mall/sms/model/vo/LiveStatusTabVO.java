package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播间状态 Tab（前端契约：live_status_tab_list[{index,name,icon}]，index 为字符串）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveStatusTabVO {

    /**
     * 状态值(空-全部;0-未开播;1-直播中;2-离开)
     */
    private String index;

    /**
     * 名称
     */
    private String name;

    /**
     * Tab 图标地址（前端 v-if="!isEmpty(item.icon)" 决定是否渲染；为空则只显示文字）
     */
    private String icon;

    public LiveStatusTabVO(String index, String name) {
        this.index = index;
        this.name = name;
    }
}
