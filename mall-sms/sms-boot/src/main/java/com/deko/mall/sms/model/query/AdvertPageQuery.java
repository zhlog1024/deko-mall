package com.deko.mall.sms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 广告分页列表查询对象
 */
@Schema(description = "广告分页查询对象")
@Data
public class AdvertPageQuery extends BasePageQuery {

    @Schema(description = "关键字")
    private String keywords;

}
