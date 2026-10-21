package com.deko.mall.oms.service.order.deduction;

/**
 * 抵扣执行阶段
 */
public enum Phase {
    /** 校验阶段：余额检查 + 可选提前扣减 */
    VALIDATE,
    /** 计价阶段：计算抵扣金额 */
    CALCULATE,
    /** 锁定阶段：实际调用资产接口扣减 */
    LOCK
}
