package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.config.FeignDecoderConfig;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * SKU 内部查询接口。跨模块读取商品档案统一通过 PMS。
 */
@FeignClient(value = "mall-pms", contextId = "sku", configuration = {FeignDecoderConfig.class})
public interface SkuFeignClient {

    /**
     * 获取商品库存信息
     */
    @GetMapping("/feign-api/v1/skus/{skuId}")
    Result<SkuInfoDTO> getSkuInfo(@PathVariable Long skuId);

    /**
     * 获取商品库存信息列表
     *
     * @param skuIds SKU ID 列表
     * @return 商品库存信息列表
     */
    @GetMapping("/feign-api/v1/skus")
    Result<List<SkuInfoDTO>> getSkuInfoList(@RequestParam List<Long> skuIds);

    /**
     * 按关键字搜索 SKU ID（名称 / SKU 编码模糊匹配）
     * <p>
     * 给库存查询、批次效期等页面的关键字筛选用：SKU 文本的属主在 pms，调用方**不应**直连
     * {@code pms_sku} 做 LIKE。返回的是 ID 集合，调用方再按 ID 过滤自己的表。
     * <p>
     * 语义约定：只做大小写不敏感的子串匹配；达到 {@code limit} 即截断（调用方应把
     * {@code limit} 视为"最多能筛出这么多"，避免无界查询打爆 pms）。
     *
     * @param keyword 关键字（空/空白串返回空集合）
     * @param limit   最多返回条数
     * @return 命中的 SKU ID 列表（可能为空）
     */
    @GetMapping("/feign-api/v1/skus/search-ids")
    Result<List<Long>> searchSkuIds(@RequestParam("keyword") String keyword,
                                    @RequestParam("limit") Integer limit);


}
