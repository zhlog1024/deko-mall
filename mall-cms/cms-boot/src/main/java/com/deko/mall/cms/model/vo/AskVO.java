package com.deko.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品问答管理列表 VO（列表不带 answer/content 大文本，详情页带）
 */
@Data
@Schema(description = "商品问答管理列表VO")
public class AskVO {

    @Schema(description = "问答ID")
    private Long id;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "商品名称快照")
    private String spuName;

    @Schema(description = "商品主图快照")
    private String spuPic;

    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "是否已回复(0否/1是)")
    private Integer isReply;

    @Schema(description = "是否启用(1是/0否)")
    private Integer isEnable;

    @Schema(description = "浏览数")
    private Integer accessCount;

    @Schema(description = "提问人昵称快照")
    private String askUser;

    @Schema(description = "提问用户ID")
    private Long askUserId;

    @Schema(description = "回答人昵称快照")
    private String replyUser;

    @Schema(description = "回答时间")
    private LocalDateTime replyTime;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
