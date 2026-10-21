package com.deko.mall.oms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单分页查询对象
 */
@Schema(description = "订单分页查询对象")
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderPageQuery extends BasePageQuery {

    /**
     * 关键字(订单编号/商品名称/会员姓名/会员手机号)
     */
    @Schema(description = "关键字(订单编号/商品名称/会员姓名/会员手机号)")
    private String keywords;

    /**
     * 订单状态（支持多个状态查询）
     */
    @Schema(description = "订单状态（支持多个状态查询）")
    private List<Integer> status;

    /**
     * 创单开始时间
     */
    @Schema(description = "开始时间(yyyy-MM-dd)", example = "2023-10-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginDate;

    /**
     * 创单截止时间
     */
    @Schema(description = "截止时间(yyyy-MM-dd)", example = "2025-10-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    /**
     * 商户号ID
     */
    @Schema(description = "商户编号")
    private String mchId;

    /**
     * 发货截止时间
     */
    @Schema(description = "发货截止时间(yyyy-MM-dd)", example = "2025-10-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryEndDate;

    /**
     * 收货开始时间
     */
    @Schema(description = "收货开始时间(yyyy-MM-dd)", example = "2023-10-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveBeginDate;

    /**
     * 收货截止时间
     */
    @Schema(description = "收货截止时间(yyyy-MM-dd)", example = "2025-10-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveEndDate;

    @Schema(description = "商品ID(SPU_ID)")
    private Long spuId;

    @Schema(description = "用户手机号")
    private String phone;

    private Long memberId;

    private Long supplierId;

    private String payChannel;

    @Schema(description = "业务支付单号")
    private String paymentNo;


    /**
     * 订单类型
     */
    @Schema(description = "订单类型")
    private String orderType;

    @Schema(description = "排除的订单场景")
    private List<String> excludeOrderScenes;

    @Schema(description = "订单场景（正向过滤）")
    private String orderScene;




}
