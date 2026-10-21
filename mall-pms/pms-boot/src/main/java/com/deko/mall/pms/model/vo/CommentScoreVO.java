package com.deko.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评论评分统计 VO
 */
@Data
public class CommentScoreVO {

    @Schema(description = "平均评分")
    private BigDecimal avg;

    @Schema(description = "评论总数")
    private Long total;
    @Schema(description = "各星级评分分布")
    private List<RatingItem> rating;
    @Data
    public static class RatingItem {

        @Schema(description = "星级名称")
        private String name;
        @Schema(description = "数量")
        private Long count;
        @Schema(description = "占比")
        private BigDecimal portion;
    }
}
