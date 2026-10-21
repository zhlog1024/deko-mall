package com.deko.mall.sms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 直播间列表项（服务间契约）
 * <p>
 * 供装修器「直播列表 / 直播选项卡」组件自动取数（{@code /api/v1/diy-designer/live/auto-list}）。
 * 字段语义与 app 端 {@code LiveRoomListVO} 一致；按项目约定 DTO 用 camelCase 出网，
 * 由 mall-admin 侧 {@code utils/live.ts} 归一到渲染契约字段（seller_name/status_name/online_count）。
 */
@Data
@Schema(description = "直播数据项")
public class LiveRoomItemDTO implements Serializable {

    @Schema(description = "直播间ID")
    private Long id;

    @Schema(description = "直播标题")
    private String title;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "主播昵称")
    private String sellerName;

    @Schema(description = "直播状态(0-未开播;1-直播中;2-离开;3-封禁)")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "在线人数")
    private Integer onlineCount;

    @Schema(description = "跳转地址(直播详情页)")
    private String url;
}
