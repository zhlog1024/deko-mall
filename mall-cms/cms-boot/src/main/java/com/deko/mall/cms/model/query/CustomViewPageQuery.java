package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义页面分页查询
 */
@Schema(description = "自定义页面分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomViewPageQuery extends BasePageQuery {

    @Schema(description = "关键字(页面名称模糊)")
    private String keywords;

    @Schema(description = "状态(1启用/0禁用)")
    private Integer isEnable;
}
