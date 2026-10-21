package com.deko.mall.cms.model.query;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DIY 页面分页查询
 * <p>
 * 同时兼容两个消费方（字段名/别名对齐装修器前端契约）：
 * 1. 装修管理列表 GET /api/v1/diy-pages/pages → 传 pageNum/pageSize/keyword（query 原名绑定）
 * 2. 装修器链接选择 POST /api/v1/diy-designer/diy/list → link-table 传 page/page_size/keywords（JSON body，
 *    经 @JsonAlias 反序列化到同字段）
 */
@Data
public class DiyPageQuery {

    @Schema(description = "页码")
    @JsonProperty("page_num")
    @JsonAlias({"page"})
    private int pageNum = 1;

    @Schema(description = "每页记录数")
    @JsonProperty("page_size")
    private int pageSize = 10;

    @Schema(description = "关键词，按名称模糊")
    @JsonAlias({"keywords"})
    private String keyword;
    @Schema(description = "启用状态（空查全部，true 仅启用，false 仅停用）")
    private Boolean isEnabled;
}
