package com.deko.mall.sms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 腾讯云直播回调事件类型
 * <p>
 * 取值对齐腾讯云官方《事件消息通知》：
 * 0-断流；1-推流；100-新录制文件；200-新截图文件。
 * <p>
 * 本期仅消费 0/1 用于驱动直播间状态；100/200 只记日志（回放/截图属二期能力）。
 */
@Getter
public enum LiveCallbackEventEnum implements IBaseEnum<Integer> {

    STREAM_END(0, "断流"),
    STREAM_BEGIN(1, "推流"),
    RECORD_FILE(100, "新录制文件"),
    SCREENSHOT_FILE(200, "新截图文件");

    private final Integer value;
    private final String label;

    LiveCallbackEventEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
