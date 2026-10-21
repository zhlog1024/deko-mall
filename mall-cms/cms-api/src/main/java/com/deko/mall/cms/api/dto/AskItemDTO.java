package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品问答 Feign 数据项（供装修器选问答/渲染等外部消费）
 * 字段语义ask 组件数据契约：标题/是否回复/回答时间/浏览数等
 */
@Data
@Schema(description = "商品问答数据项")
public class AskItemDTO implements Serializable {

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
    @Schema(description = "回答人昵称快照")
    private String replyUser;
    @Schema(description = "回答时间")
    private LocalDateTime replyTime;

    @Schema(description = "提问人昵称快照")
    private String askUser;
    @Schema(description = "浏览数")
    private Integer accessCount;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
