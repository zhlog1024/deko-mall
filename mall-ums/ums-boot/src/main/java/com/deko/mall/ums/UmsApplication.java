package com.deko.mall.ums;

import com.deko.mall.oms.api.OrderFeignClient;
import com.deko.mall.pms.api.SpuFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableFeignClients(basePackageClasses = {SpuFeignClient.class, OrderFeignClient.class})
public class UmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(UmsApplication.class, args);
    }
}
