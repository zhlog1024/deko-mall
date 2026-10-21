package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 视频新增/编辑 DTO（管理端运营上传 PGC 内容）
 */
@Data
@Schema(description = "视频新增/编辑DTO")
public class VideoSaveDTO {

    @Schema(description = "视频ID(编辑时传)")
    private Long id;

    @NotBlank(message = "视频标题不能为空")
    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "封面图URL")
    private String cover;

    @NotBlank(message = "视频地址不能为空")
    @Schema(description = "视频地址URL(mp4/hls)")
    private String videoUrl;

    @Schema(description = "时长(秒),默认0")
    private Integer duration;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "是否推荐(1是/0否),默认0")
    private Integer isRecommended;

    @Schema(description = "是否热门(1是/0否),默认0")
    private Integer isHot;

    @Schema(description = "排序(升序),默认0")
    private Integer sort;

    @Schema(description = "是否启用(1是/0否),默认1")
    private Integer isEnable;

    /**
     * 关联商品SPU ID(管理端选品写入;0/空=不挂)
     */
    private Long spuId;

    /**
     * 商品名称快照
     */
    private String spuName;

    /**
     * 商品主图快照
     */
    private String spuPic;

    /**
     * 商品价格快照
     */
    private java.math.BigDecimal spuPrice;
}
