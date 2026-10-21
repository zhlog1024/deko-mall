package com.deko.mall.oms.enums;

import com.deko.mall.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 售后状态枚举
 */
@Getter
public enum AfterSaleStatusEnum implements IBaseEnum<Integer> {

    WAIT_AUDIT(1, "待审核"),          // 用户提交申请，等待商家审核
    AUDIT_REJECTED(2, "已拒绝"),      // 商家拒绝售后申请
    WAIT_RETURN(3, "待退货"),         // 商家同意申请，等待用户退货
    RETURNING(4, "退货中"),           // 用户已填写物流信息，正在退货
    WAIT_RECEIVE(5, "待收货"),
    WAIT_REFUND(6, "待退款"),// 用户已发货，等待商家收货
    REFUNDING(7, "退款中"),           // 退款处理中
    REFUND_SUCCESS(8, "退款成功"),    // 退款已到账
    REFUND_FAIL(9, "退款失败"),       // 退款失败
    CANCELED(10, "已取消");            // 用户取消售后

    @Getter
    private Integer value;
    @Getter
    private String label;

    AfterSaleStatusEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

}
