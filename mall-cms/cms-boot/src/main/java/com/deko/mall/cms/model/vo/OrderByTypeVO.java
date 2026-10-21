package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 排序类型 VO
 */
@Data
@Schema(description = "排序类型")
public class OrderByTypeVO {

    @Schema(description = "排序值")
    private String value;

    @Schema(description = "排序名称")
    private String name;
}
