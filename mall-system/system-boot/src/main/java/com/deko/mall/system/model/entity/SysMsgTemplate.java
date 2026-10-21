package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * 消息模板实体
 */
@Data
@TableName("sys_msg_template")
public class SysMsgTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板内容
     */
    private String templateContent;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 第三方模板ID
     */
    private String thirdTemplateId;

    /**
     * 模板参数（JSON格式，[{"name":"code","description":"验证码"}]）
     */
    private String templateParams;

    /**
     * 模板状态(0-禁用 1-启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
