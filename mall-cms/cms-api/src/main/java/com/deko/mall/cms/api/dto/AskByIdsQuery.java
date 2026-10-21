package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品问答指定数据查询（Feign，装修器"指定问答"回显）
 */
@Data
@Schema(description = "商品问答指定数据查询")
public class AskByIdsQuery implements Serializable {

    @Schema(description = "问答ID列表")
    private List<Long> ids;
}
