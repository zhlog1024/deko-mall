package com.deko.mall.ums.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 银行列表配置
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "mall.bank-list")
public class BankListConfig {

    /**
     * 银行列表
     */
    private List<String> banks = Arrays.asList(
            "中国工商银行", "中国农业银行", "中国银行", "中国建设银行",
            "交通银行", "招商银行", "中国民生银行", "中国光大银行",
            "中国邮政储蓄银行", "中信银行", "华夏银行", "平安银行",
            "浦发银行", "兴业银行", "广发银行", "北京银行",
            "上海银行", "江苏银行", "宁波银行", "杭州银行", "温州银行", "金华银行", "台州银行",
            "泰隆银行", "民泰银行"
    );
}

