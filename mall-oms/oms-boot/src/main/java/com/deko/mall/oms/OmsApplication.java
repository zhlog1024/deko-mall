package com.deko.mall.oms;

import com.deko.mall.pay.api.client.PayFeignClient;
import com.deko.mall.pms.api.SkuFeignClient;
import com.deko.mall.system.api.SystemFeignClient;
import com.deko.mall.ums.api.MemberFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackageClasses = {MemberFeignClient.class, SkuFeignClient.class, SystemFeignClient.class, PayFeignClient.class})
public class OmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmsApplication.class, args);
    }

}
