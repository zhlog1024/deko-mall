package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DIY装修器配置 VO
 */
@Data
@Schema(description = "DIY装修器配置")
public class DiyConfigVO {

    @JsonProperty("site_name")
    @Schema(description = "站点名称")
    private String siteName;

    @JsonProperty("site_logo")
    @Schema(description = "站点Logo")
    private String siteLogo;

    @JsonProperty("currency_symbol")
    @Schema(description = "货币符号")
    private String currencySymbol;

    @JsonProperty("attachment_host")
    @Schema(description = "附件访问地址")
    private String attachmentHost;

    @JsonProperty("diy_detail_url")
    @Schema(description = "DIY详情URL")
    private String diyDetailUrl;

    @JsonProperty("diy_save_url")
    @Schema(description = "DIY保存URL")
    private String diySaveUrl;

    @JsonProperty("app_tabbar_data_url")
    @Schema(description = "底部导航数据URL")
    private String appTabbarDataUrl;

    @JsonProperty("app_tabbar_save_url")
    @Schema(description = "底部导航保存URL")
    private String appTabbarSaveUrl;

    @JsonProperty("preview_url")
    @Schema(description = "装修预览地址（H5端页面地址）")
    private String previewUrl;

    @JsonProperty("diy_config_operate")
    @Schema(description = "装修器操作权限配置")
    private DiyConfigOperateVO diyConfigOperate;

    @JsonProperty("attachment_operate")
    @Schema(description = "附件权限配置")
    private AttachmentOperateVO attachmentOperate;

    @JsonProperty("attachment_category_operate")
    @Schema(description = "附件分类权限配置")
    private AttachmentCategoryOperateVO attachmentCategoryOperate;
}
