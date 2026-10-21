package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 页面链接分组 VO
 */
@Data
@Schema(description = "页面链接分组")
public class PageLinkGroupVO {

    @Schema(description = "分组名称")
    private String name;
    @Schema(description = "分组类型")
    private String type;
    @Schema(description = "是否显示")
    private Integer isShow;
    @Schema(description = "链接地址")
    private String url;
    @Schema(description = "子数据列表")
    private List<PageLinkVO> data;
}
