package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App端 DIY 页面数据响应 VO
 * 与
 */
@Data
@Schema(description = "App端DIY页面数据响应")
public class DiyPageAppVO {

    @Schema(description = "页面数据")
    private DiyPageDataVO data;

    @Schema(description = "是否使用缓存结果")
    private Integer isResultDataCache = 0;

    @Data
    @Schema(description = "页面详细数据")
    public static class DiyPageDataVO {
        
        @Schema(description = "页面ID")
        private Long id;
        @Schema(description = "名称")
        private String name;
        @Schema(description = "描述")
        private String describe;
        @Schema(description = "Logo")
        private String logo;
        @Schema(description = "装修配置（已解析为对象）")
        private Object config;

        @Schema(description = "封面图")
        private String coverUrl;
        @Schema(description = "是否首页")
        private Boolean isHome;
        @Schema(description = "是否启用")
        private Boolean isEnabled;
    }
}
