package com.deko.mall.pay.service.pay;

import com.deko.mall.pay.api.dto.PaymentRequest;
import com.deko.mall.pay.api.dto.RefundRequestDTO;
import com.deko.mall.pay.api.dto.RefundResultDTO;
import com.deko.mall.pay.model.vo.CashierVO;
import com.deko.mall.pay.model.vo.PaymentResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import com.deko.mall.pay.model.dto.PayActionDTO;

/**
 * 订单支付服务接口
 * 提供订单支付相关的核心功能，包括：
 * 1. 支付流程管理（创建支付单、查询状态、关闭支付）
 * 2. 退款流程管理（申请退款、处理退款回调）
 * 3. 支付渠道管理（支持多种支付方式）
 * 4. 收银台服务（获取支付信息和展示）
 */
public interface PayService {

    /**
     * 获取收银台信息
     * 根据支付单号和设备类型返回对应的收银台展示数据
     *
     * @param paymentNo  支付单号
     * @param deviceType 设备类型（APP、H5、PC等）
     * @return 收银台信息，包括订单信息、可用支付方式等
     */
    CashierVO getCashierInfo(String paymentNo, String deviceType);

    /**
     * 发起支付
     *
     * @param dto 支付行为参数（渠道、支付单号、授权码等）
     * @return 支付参数，包括支付链接、二维码等支付渠道要求的参数
     */
    PaymentResponseVO paymentRequest(PayActionDTO dto);

    /**
     * 根据退款DTO执行退款操作
     *
     * @param refundRequestDTO 退款参数DTO
     * @return 退款申请结果，true表示申请成功
     */
    boolean refundRequest(RefundRequestDTO refundRequestDTO);

    /**
     * 处理支付回调
     *
     * @param payChannel 支付渠道
     * @param request    HTTP请求对象
     * @return 处理结果，true表示处理成功
     */
    boolean paymentResult(String payChannel, HttpServletRequest request);

    /**
     * 处理退款回调
     *
     * @param refundResultDTO 退款回调数据
     * @return 处理结果，true表示处理成功
     */
    boolean refundResult(RefundResultDTO refundResultDTO);

    /**
     * 处理退款回调请求
     *
     * @param channel 支付渠道
     * @param request HTTP请求对象
     * @return 退款结果DTO
     */
    RefundResultDTO refundCallback(String channel, HttpServletRequest request);

    /**
     * 查询支付状态
     *
     * @param payNo 支付单号
     * @return 支付状态信息
     */
    PaymentResponseVO queryStatus(String payNo);

    /**
     * 查询支付单状态（按业务支付单号，供前端轮询）
     *
     * @param paymentNo 业务支付单号
     * @return 支付状态信息
     */
    PaymentResponseVO getPaymentStatus(String paymentNo);

    /**
     * 关闭支付单
     *
     * @param payNo 支付单号
     * @return 关闭结果
     */
    boolean closePayment(String payNo);

    /** 更新支付金额 */
    boolean updatePaymentAmount(PaymentRequest request);

    /** 关闭支付（按业务支付单号，关闭所有未完成的渠道请求） */
    void closePaymentByPaymentNo(String paymentNo);

    /** 回退支付单状态到待支付 */
    void resetPaymentToWaitPay(String paymentNo);
}
