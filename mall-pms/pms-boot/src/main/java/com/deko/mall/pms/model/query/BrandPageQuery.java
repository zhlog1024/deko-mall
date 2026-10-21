package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 品牌分页列表查询对象
 */
@Schema(description = "品牌分页查询对象")
@Data
public class BrandPageQuery extends BasePageQuery {

    @Schema(description = "关键字")
    private String keywords;

}
