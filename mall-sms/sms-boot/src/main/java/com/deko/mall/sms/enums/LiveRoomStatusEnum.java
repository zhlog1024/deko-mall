package com.deko.mall.sms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 直播间状态枚举
 * <p>
 * 状态值与前端契约对齐：0-未开播(离线)；1-直播中(在线)；2-主播离开；3-封禁
 */
@Getter
public enum LiveRoomStatusEnum implements IBaseEnum<Integer> {

    NOT_STARTED(0, "未开播"),
    LIVING(1, "直播中"),
    AWAY(2, "离开"),
    BANNED(3, "封禁");

    private final Integer value;
    private final String label;
    LiveRoomStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}
