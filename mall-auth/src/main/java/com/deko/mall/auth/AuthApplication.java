package com.deko.mall.auth;

import com.deko.mall.pms.api.SupplierFeignClient;
import com.deko.mall.system.api.SystemFeignClient;
import com.deko.mall.ums.api.MemberFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackageClasses = {SystemFeignClient.class, MemberFeignClient.class, SupplierFeignClient.class})
@SpringBootApplication
@EnableDiscoveryClient
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
