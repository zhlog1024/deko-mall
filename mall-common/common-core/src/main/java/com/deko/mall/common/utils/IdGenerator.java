package com.deko.mall.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 业务ID生成器
 * <p>
 * 生成规则：业务前缀+年月日+流水序号
 * 例如：PAY2025060312345678
 * </p>
 */
@Component
@Slf4j
public class IdGenerator {

    @Value("${spring.cloud.nacos.discovery.metadata.node-id:1}")
    private Long workerId;

    private Snowflake snowflake;

    @PostConstruct
    public void init() {
        // 数据中心ID使用0，workerId使用配置的节点ID
        snowflake = IdUtil.createSnowflake(0, workerId);
    }

    /**
     * 生成支付单号
     * 格式：PAY+年月日+流水序号
     * 例如：PAY2025060312345678
     *
     * @return 支付单号
     */
    public String generatePayNo() {
        return generateBusinessNo("PAY");
    }

    /**
     * 生成订单号
     * 格式：OD+年月日+流水序号
     * 例如：OD2025060312345678
     *
     * @return 订单号
     */
    public String generateOrderNo() {
        return generateBusinessNo("OD");
    }

    /**
     * 生成退款单号
     * 格式：RAF+年月日+流水序号
     * 例如：RAF2025060312345678
     *
     * @return 退款单号
     */
    public String generateRefundNo() {
        return generateBusinessNo("RAF");
    }

    /**
     * 生成支付流水号（渠道支付请求号）
     * 格式：PYT+年月日+流水序号
     * 例如：PYT2025060312345678
     *
     * @return 支付流水号
     */
    public String generatePayTransactionNo() {
        return generateBusinessNo("PYT");
    }

    /**
     * 生成退款流水号（渠道退款请求号）
     * 格式：RFT+年月日+流水序号
     * 例如：RFT2025060312345678
     *
     * @return 退款流水号
     */
    public String generateRefundTransactionNo() {
        return generateBusinessNo("RFT");
    }

    /**
     * 生成物流单号
     * 格式：SH+年月日+流水序号
     * 例如：SH2025060312345678
     *
     * @return 物流单号
     */
    public String generateShipmentNo() {
        return generateBusinessNo("SH");
    }

    /**
     * 生成售后单号
     * 格式：AS+年月日+流水序号
     * 例如：AS2025060312345678
     *
     * @return 售后单号
     */
    public String generateAfterSaleNo() {
        return generateBusinessNo("AS");
    }

    /**
     * 生成提现申请单号
     * 格式：WD+年月日+流水序号
     * 例如：WD2025060312345678
     *
     * @return 提现申请单号
     */
    public String generateWithdrawalNo() {
        return generateBusinessNo("WD");
    }


    /**
     * 生成结算单号
     * 格式：WD+年月日+流水序号
     * 例如：WD2025060312345678
     *
     * @return 提现申请单号
     */
    public String generateSettlementNo() {
        return generateBusinessNo("ST");
    }

    /**
     * 生成资产交易流水号
     * 格式：T+年月日+流水序号
     * 例如：T2025060312345678
     *
     * @return 资产交易流水号
     */
    public String generateAssetTransactionNo() {
        return generateBusinessNo("TR");
    }

    /**
     * 生成扣款单号
     * 格式：DD+年月日+流水序号
     * 例如：DD2025060312345678
     *
     * @return 扣款单号
     */
    public String generateDeductionNo() {
        return generateBusinessNo("DD");
    }

    /**
     * 通用业务编号生成方法
     *
     * @param prefix 业务前缀
     * @return 业务编号
     */
    private String generateBusinessNo(String prefix) {
        // 获取当前日期，格式为yyMMdd (250603)
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");

        // 获取雪花算法生成的ID并截取后8位
        String snowflakeId = snowflake.nextIdStr();
        String shortId = snowflakeId.substring(Math.max(0, snowflakeId.length() - 8));

        // 拼接业务编号
        String businessNo = prefix + dateStr + shortId;

        log.debug("生成业务编号: {}", businessNo);
        return businessNo;
    }
}
