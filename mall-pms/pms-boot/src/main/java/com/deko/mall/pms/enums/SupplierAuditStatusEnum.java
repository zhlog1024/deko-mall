package com.deko.mall.pms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 供应商审核状态枚举
 */
public enum SupplierAuditStatusEnum implements IBaseEnum<Integer> {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 审核通过
     */
    APPROVED(1, "审核通过"),

    /**
     * 审核拒绝
     */
    REJECTED(2, "审核拒绝");

    @Getter
    private final Integer value;

    @Getter
    private final String label;

    SupplierAuditStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
