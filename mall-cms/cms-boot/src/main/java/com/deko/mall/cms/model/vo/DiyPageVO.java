package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class DiyPageVO {

    @Schema(description = "页面ID")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "装修JSON")
    private String config;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "是否首页")
    private Boolean isHome;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

