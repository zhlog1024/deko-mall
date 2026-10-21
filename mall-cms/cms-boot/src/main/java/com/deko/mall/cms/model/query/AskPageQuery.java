package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品问答分页查询（管理端）
 */
@Schema(description = "商品问答分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class AskPageQuery extends BasePageQuery {

    @Schema(description = "关键字(问题标题模糊)")
    private String keyword;

    @Schema(description = "商品SPU ID(精确,空=全部)")
    private Long spuId;

    @Schema(description = "是否已回复(1是/0否,空=全部)")
    private Integer isReply;

    @Schema(description = "是否启用(1是/0否,空=全部)")
    private Integer isEnable;

    @Schema(description = "排序字段:createTime=创建时间倒序;空或sort=自定义排序优先(升序)+创建时间倒序")
    private String sortField;
}
