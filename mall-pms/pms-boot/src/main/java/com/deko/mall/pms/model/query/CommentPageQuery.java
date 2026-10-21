package com.deko.mall.pms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentPageQuery extends BasePageQuery {

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "状态 0-待审核 1-已通过 2-已拒绝")
    private Integer status;

    @Schema(description = "评分")
    private Integer rating;

    @Schema(description = "会员昵称")
    private String memberName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
