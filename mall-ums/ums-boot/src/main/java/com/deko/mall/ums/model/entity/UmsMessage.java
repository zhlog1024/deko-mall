package com.deko.mall.ums.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * 会员消息实体
 */
@Data
public class UmsMessage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private String title;

    private String detail;

    private Integer type;

    private Integer isRead;

    private Integer userDeleted;

    private Integer deleted;
}
