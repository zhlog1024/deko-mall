package com.deko.mall.pms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评论 VO
 */
@Data
public class GoodsCommentVO {

    @Schema(description = "评论ID")
    private Long id;
    @Schema(description = "商品SPU ID")
    private Long spuId;
    @Schema(description = "SKU ID")
    private Long skuId;
    @Schema(description = "订单ID")
    private Long orderId;
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "会员ID")
    private Long memberId;
    @Schema(description = "会员昵称")
    private String memberName;
    @Schema(description = "会员头像")
    private String memberAvatar;
    @Schema(description = "评分 1-5")
    private Integer rating;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "评论图片")
    private List<String> images;
    @Schema(description = "是否匿名")
    private Integer isAnonymous;
    @Schema(description = "是否已回复")
    private Integer isReplied;
    @Schema(description = "回复内容")
    private String replyContent;
    @Schema(description = "回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    @Schema(description = "状态 0-待审核 1-已通过 2-已拒绝")
    private Integer status;
    @Schema(description = "SKU名称")
    private String skuName;
    @Schema(description = "商品名称")
    private String spuName;
    @Schema(description = "商品图片")
    private String spuPic;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
