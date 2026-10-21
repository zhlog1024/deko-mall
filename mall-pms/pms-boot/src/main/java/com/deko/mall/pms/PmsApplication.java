package com.deko.mall.pms;

import com.deko.mall.oms.api.OrderFeignClient;
import com.deko.mall.pay.api.client.PayFeignClient;
import com.deko.mall.system.api.SystemFeignClient;
import com.deko.mall.ums.api.FavorFeignClient;
import com.deko.mall.ums.api.GoodsBrowseFeignClient;
import com.deko.mall.ums.api.MemberFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackageClasses = {MemberFeignClient.class, SystemFeignClient.class, OrderFeignClient.class, GoodsBrowseFeignClient.class, FavorFeignClient.class, PayFeignClient.class})
public class PmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PmsApplication.class, args);
    }
}
