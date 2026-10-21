package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章指定数据查询（Feign，装修器"指定数据"模式）
 */
@Data
@Schema(description = "文章指定数据查询")
public class ArticleByIdsQuery implements Serializable {

    @Schema(description = "文章ID列表")
    private List<Long> ids;
}
