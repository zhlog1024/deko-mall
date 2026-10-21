package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 品牌项 VO
 */
@Data
@Schema(description = "品牌项")
public class BrandVO {

    @Schema(description = "品牌ID")
    private Long id;
    @Schema(description = "品牌名称")
    private String name;
    @Schema(description = "品牌Logo")
    @JsonProperty("logo")  // 序列化输出 logo
    private String logoUrl;
}
