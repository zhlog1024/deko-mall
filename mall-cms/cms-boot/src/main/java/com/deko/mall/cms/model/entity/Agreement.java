package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 协议实体，通过 document 标识定位。
 */
@Data
@TableName("cms_agreement")
@EqualsAndHashCode(callSuper = true)
public class Agreement extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 协议标识(userregister/userprivacy/...)
     */
    private String document;

    /**
     * 协议标题
     */
    private String title;

    /**
     * 协议内容(HTML富文本)
     */
    private String content;

    /**
     * 状态(1启用/0禁用)
     */
    private Integer status;

    /**
     * 版本号(预留,历史留痕二期)
     */
    private Integer version;

    /**
     * 生效时间(预留)
     */
    private LocalDateTime effectiveTime;

    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer deleted;
}
