package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * 直播页基础信息（前端契约：base{seo_title,seo_desc,application_name,header_logo}）
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class LiveBaseVO {

    /**
     * SEO标题
     */
    private String seoTitle;

    /**
     * SEO描述
     */
    private String seoDesc;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 头部Logo
     */
    private String headerLogo;
}
