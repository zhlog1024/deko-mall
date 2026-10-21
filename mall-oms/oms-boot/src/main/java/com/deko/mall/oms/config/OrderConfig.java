package com.deko.mall.oms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 订单相关配置
 */
@RefreshScope
@Component
@ConfigurationProperties(prefix = "mall.order")
@Data
public class OrderConfig {




    /**
     * 积分抵扣开关，默认开启，Nacos 配置 mall.order.points-deduction-enabled=false 可关闭
     */
    private Boolean pointsDeductionEnabled = true;
}
