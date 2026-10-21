package com.deko.mall.oms.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * 订单操作历史记录
 *
 * @author zhlog1024
 */
@Data
public class OmsOrderLog extends BaseEntity {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;
    /**
     * 操作人[用户；系统；后台管理员]
     */
    private String operateUser;
    /**
     * 操作详情
     */
    private String detail;
    /**
     * 记录操作前的状态
     */
    private Integer orderStatus;


}
