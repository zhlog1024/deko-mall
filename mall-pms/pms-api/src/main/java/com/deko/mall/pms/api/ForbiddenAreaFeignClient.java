package com.deko.mall.pms.api;

import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 禁售区域服务Feign客户端
 */
@FeignClient(value = "mall-pms", contextId = "forbiddenArea")
public interface ForbiddenAreaFeignClient {

    /**
     * 批量检查商品在指定区域是否禁售
     *
     * @param request 请求参数，包含商品SPU ID列表和地区信息
     * @return 禁售的商品SPU ID列表
     */
    @PostMapping("/feign-api/v1/forbidden-areas/batch-check")
    Result<List<Long>> batchCheckForbiddenProducts(@RequestBody ForbiddenAreaCheckRequest request);

    /**
     * 检查区域编码是否在指定的禁售区域列表中
     *
     * @param regionCode       区域编码
     * @param forbiddenAreaIds 禁售区域ID列表
     * @return 包含该区域编码的禁售区域ID列表
     */
    @GetMapping("/feign-api/v1/forbidden-areas/check-region")
    Result<List<Long>> checkRegionInForbiddenAreas(@RequestParam("regionCode") String regionCode,
                                                   @RequestParam("forbiddenAreaIds") List<Long> forbiddenAreaIds);

    /**
     * 批量检查商品是否禁售的请求参数
     */
    class ForbiddenAreaCheckRequest {
        private List<Long> spuIds;
        private Integer provinceId;
        private Integer cityId;
        private Integer areaId;
        // Getters and Setters
        public List<Long> getSpuIds() {
            return spuIds;
        }

        public void setSpuIds(List<Long> spuIds) {
            this.spuIds = spuIds;
        }

        public Integer getProvinceId() {
            return provinceId;
        }

        public void setProvinceId(Integer provinceId) {
            this.provinceId = provinceId;
        }

        public Integer getCityId() {
            return cityId;
        }

        public void setCityId(Integer cityId) {
            this.cityId = cityId;
        }

        public Integer getAreaId() {
            return areaId;
        }

        public void setAreaId(Integer areaId) {
            this.areaId = areaId;
        }
    }
}
