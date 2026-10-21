package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签选项VO
 */
@Schema(description = "标签选项VO")
@Data
public class TagOptionVO {

    @Schema(description = "标签值")
    private String value;

    @Schema(description = "标签描述")
    private String label;
}
