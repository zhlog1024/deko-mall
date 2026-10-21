package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Builder;
import lombok.Data;

/**
 * 消息记录实体
 */
@Data
@Builder
@TableName("sys_msg_record")
public class SysMsgRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String channelCode;

    private String msgType;

    private String templateCode;

    private String templateParams;

    private String receiver;

    private String content;

    private Integer status;

    private String errorMsg;


}
