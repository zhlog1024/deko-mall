package com.deko.mall.oms.service.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.oms.model.entity.order.OmsOrder;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSale;
import com.deko.mall.oms.model.form.OrderAfterSaleForm;
import com.deko.mall.oms.model.form.ReturnLogisticsForm;
import com.deko.mall.oms.model.query.AfterSalePageQuery;
import com.deko.mall.oms.model.vo.AfterSalePageVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单售后服务接口
 */
public interface OrderAfterSaleService extends IService<OmsOrderAfterSale> {

    /**
     * 申请售后
     *
     * @param form  售后申请表单
     * @param order 订单实体（由门面层提供）
     * @return 售后ID
     */
    Long applyAfterSale(OrderAfterSaleForm form, OmsOrder order);

    /**
     * 审核售后申请
     *
     * @param id       售后申请ID
     * @param approve  是否通过
     * @param remark   审核备注
     * @param handleBy 处理人
     */
    void auditAfterSale(Long id, boolean approve, String remark, String handleBy);

    /**
     * 填写退货物流信息
     *
     * @return 操作结果
     */
    boolean returnGoods(ReturnLogisticsForm returnLogisticsForm);

    /**
     * 更新售后单退款状态
     *
     * @param afterSale  售后单
     * @param status     退款状态
     * @param refundTime 退款时间，如果为成功状态需提供
     * @return 操作结果
     */
    boolean updateAfterSaleRefundStatus(OmsOrderAfterSale afterSale, Integer status, LocalDateTime refundTime);

    /**
     * 取消售后
     *
     * @param id 售后ID
     * @return 操作结果
     */
    boolean cancelAfterSale(Long id);

    /**
     * 分页查询售后列表
     *
     * @param queryParams 查询参数
     * @return 分页结果
     */
    Page<AfterSalePageVO> getRefundPage(AfterSalePageQuery queryParams);

    /**
     * 检查是否存在对应的售后退款记录
     * 通过订单号和退款单号判断是否为售后退款
     *
     * @param orderNo  订单号
     * @param refundNo 退款单号
     * @return 是否为售后退款
     */
    boolean checkAfterSaleRefund(String orderNo, String refundNo);


    /**
     * 查询售后详情
     *
     * @param afterSaleId 售后ID
     * @return 售后详情对象
     */
    AfterSalePageVO getAfterSaleDetail(Long afterSaleId);

    /**
     * 分页查询售后列表
     *
     * @param queryParams 查询参数
     * @return 分页结果
     */
    Page<AfterSalePageVO> getAfterSalePage(AfterSalePageQuery queryParams);

    /**
     * 创建售后单并自动审核通过（用于快速退款/支付异常退款场景）
     *
     * @param order        订单实体
     * @param refundType   退款类型（1-仅退款，2-退货退款）
     * @param refundAmount 退款金额
     * @param reason       退款原因
     * @return 售后单ID
     */
    Long applyAfterSaleAutoApprove(OmsOrder order, Integer refundType, BigDecimal refundAmount, String reason);

    /**
     * 查询订单已退款总额（不含已取消的售后单）
     */
    BigDecimal sumRefundedAmount(Long orderId);

}
