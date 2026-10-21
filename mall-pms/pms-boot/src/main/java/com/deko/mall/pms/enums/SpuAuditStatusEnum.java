package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

public enum SpuAuditStatusEnum implements IBaseEnum<Integer> {

    // 待审核
    PENDING_AUDIT(0, "待审核"),

    // 审核通过
    AUDIT_SUCCESS(1, "审核通过"),

    // 审核拒绝
    AUDIT_REJECTED(2, "审核拒绝");

    @Getter
    private final Integer value;

    @Getter
    private final String label;

    SpuAuditStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}