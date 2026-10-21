package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.dto.FreightCalcParam;
import com.deko.mall.pms.model.dto.FreightTemplateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 运费模板服务Feign客户端
 */
@FeignClient(value = "mall-pms", contextId = "freight")
public interface FreightFeignClient {

    /**
     * 根据ID集合批量获取运费模板
     *
     * @param ids 运费模板ID集合
     * @return 运费模板列表
     */
    @GetMapping("/feign-api/v1/freight/templates/batch")
    Result<List<FreightTemplateDTO>> getFreightTemplatesByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 计算运费
     *
     * @param param 运费计算参数
     * @return 运费金额(单位：元)
     */
    @PostMapping("/feign-api/v1/freight/calculate")
    Result<BigDecimal> calculateFreight(@RequestBody FreightCalcParam param);

    /**
     * 批量计算运费
     *
     * @param paramMap 运费模板ID与运费计算参数的映射
     * @return 运费模板ID与运费金额的映射(单位：元)
     */
    @PostMapping("/feign-api/v1/freight/batch-calculate")
    Result<Map<Long, BigDecimal>> batchCalculateFreight(@RequestBody Map<Long, FreightCalcParam> paramMap);
}
