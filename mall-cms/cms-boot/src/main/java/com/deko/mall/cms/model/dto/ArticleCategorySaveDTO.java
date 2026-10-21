package com.deko.mall.cms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文章分类新增/编辑 DTO
 */
@Data
@Schema(description = "文章分类新增/编辑DTO")
public class ArticleCategorySaveDTO {

    @Schema(description = "分类ID(编辑时传)")
    private Long id;

    @Schema(description = "父分类id(0=顶级),默认0")
    private Long pid;

    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序(升序),默认0")
    private Integer sort;

    @Schema(description = "是否启用(1是/0否),默认1")
    private Integer isEnable;
}
