package com.deko.mall.pms.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交评论表单
 */
@Data
public class CommentSubmitForm {

    @Schema(description = "评论项列表")
    @NotNull(message = "评论项不能为空")
    private List<CommentItem> items;
    @Schema(description = "是否匿名")
    private Integer isAnonymous;
    @Data
    public static class CommentItem {

        @Schema(description = "订单商品项ID")
        @NotNull(message = "订单商品项ID不能为空")
        private Long orderItemId;
        @Schema(description = "商品SPU ID")
        @NotNull(message = "商品ID不能为空")
        private Long spuId;
        @Schema(description = "SKU ID")
        private Long skuId;
        @Schema(description = "评分")
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低1分")
        @Max(value = 5, message = "评分最高5分")
        private Integer rating;
        @Schema(description = "评论内容")
        @NotBlank(message = "评论内容不能为空")
        private String content;
        @Schema(description = "评论图片")
        private List<String> images;
    }
}
