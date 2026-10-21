package com.deko.mall.cms.api;

import com.deko.mall.cms.api.dto.CustomViewItemDTO;
import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 自定义页面 Feign 客户端（供装修桥接、未来官网等消费）
 */
@FeignClient(value = "mall-cms", contextId = "cmsCustomView")
public interface CmsCustomViewFeignClient {

    /**
     * 启用中的自定义页面列表（装修器"自定义页面"链接选择用）
     *
     * @return 自定义页面数据项
     */
    @GetMapping("/feign-api/v1/custom-views/enabled-list")
    Result<List<CustomViewItemDTO>> listEnabled();
}
