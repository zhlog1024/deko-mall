package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 协议分页查询（T1）
 */
@Schema(description = "协议分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgreementPageQuery extends BasePageQuery {

    @Schema(description = "关键字(标识/标题模糊)")
    private String keywords;
}
