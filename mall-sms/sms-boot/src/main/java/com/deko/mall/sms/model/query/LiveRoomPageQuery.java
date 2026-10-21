package com.deko.mall.sms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 直播间分页查询参数（管理端）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LiveRoomPageQuery extends BasePageQuery {

    /**
     * 关键词（标题/主播/直播间编号）
     */
    @Schema(description = "关键词")
    private String keywords;

    /**
     * 直播状态(0-未开播;1-直播中;2-离开;3-封禁)
     */
    @Schema(description = "直播状态")
    private Integer status;
}
