package com.deko.mall.cms;

import com.deko.mall.oms.api.OrderFeignClient;
import com.deko.mall.pms.api.BrandFeignClient;
import com.deko.mall.pms.api.CategoryFeignClient;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.sms.api.LiveRoomFeignClient;
import com.deko.mall.system.api.SysAttachmentFeignClient;
import com.deko.mall.ums.api.MemberFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 内容服务启动类。
 * <p>
 * 承载内容域：内容供给（文章/视频/问答/协议/自定义页）+ 内容编排（DIY 装修页面/楼层/tabbar）。
 * 原 mall-diy 已合并至此 —— DIY 对内容的调用改为同进程直调 Service，
 * 仅保留对商品(pms)/营销(sms)/会员(ums)/订单(oms)/系统(system)的跨域 Feign 调用。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackageClasses = {
        CategoryFeignClient.class, MemberFeignClient.class, BrandFeignClient.class,
        SpuFeignClient.class, OrderFeignClient.class, SysAttachmentFeignClient.class,
        LiveRoomFeignClient.class
})
public class CmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }
}
