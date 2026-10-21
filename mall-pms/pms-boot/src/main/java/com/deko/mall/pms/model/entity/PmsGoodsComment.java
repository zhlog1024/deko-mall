package com.deko.mall.pms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 商品评论实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PmsGoodsComment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spuId;

    private Long skuId;

    private Long orderId;

    private String orderNo;

    private Long memberId;

    private String memberName;

    private String memberAvatar;

    private Integer rating;

    private String content;

    private String images;

    private Integer isAnonymous;

    private Integer isReplied;

    private String replyContent;

    private LocalDateTime replyTime;

    private Integer status;

    private Integer deleted;
}
