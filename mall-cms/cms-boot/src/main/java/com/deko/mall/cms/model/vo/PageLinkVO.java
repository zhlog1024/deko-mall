package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 页面链接项 VO
 */
@Data
@Schema(description = "页面链接项")
public class PageLinkVO {

    @Schema(description = "链接名称")
    private String name;
    @Schema(description = "链接类型")
    private String type;
    @Schema(description = "是否显示")
    @JsonProperty("is_show")  // 序列化输出 is_show
    private Integer isShow;
    @Schema(description = "链接地址")
    private String url;
    @Schema(description = "链接页面")
    private String page;
    @Schema(description = "子数据")
    private Object data;
}
