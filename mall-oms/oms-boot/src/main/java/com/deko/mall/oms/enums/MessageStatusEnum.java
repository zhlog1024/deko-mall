package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
public enum MessageStatusEnum implements IBaseEnum<Integer> {

    PENDING(0, "待发送"),
    SENT(1, "已发送"),
    PROCESSED(2, "已处理"),
    FAILED(3, "发送失败");

    private final Integer value;
    private final String label;

    MessageStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
