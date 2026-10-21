package com.deko.mall.pms.controller.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.BrandFeignClient;
import com.deko.mall.pms.model.dto.PmsBrandDTO;
import com.deko.mall.pms.model.entity.PmsBrand;
import com.deko.mall.pms.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Feign-品牌控制层
 */
@Tag(name = "feign-品牌接口")
@RestController
@RequestMapping("/feign-api/v1/brands")
@AllArgsConstructor
@Slf4j
public class BrandFeignController implements BrandFeignClient {

    private BrandService brandService;

    @Operation(summary = "获取品牌列表（简单信息）")
    @GetMapping("/simple")
    @Override
    public Result<List<PmsBrandDTO>> getBrandList() {
        List<PmsBrand> list = brandService.list(new LambdaQueryWrapper<PmsBrand>()
                .select(PmsBrand::getId, PmsBrand::getName, PmsBrand::getLogoUrl, PmsBrand::getSort)
                .orderByAsc(PmsBrand::getSort));
        
        // 转换为DTO
        List<PmsBrandDTO> dtoList = list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return Result.success(dtoList);
    }

    @Operation(summary = "根据ID列表获取品牌")
    @GetMapping
    @Override
    public Result<List<PmsBrandDTO>> getBrandsByIds(@RequestParam List<Long> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return Result.success(List.of());
        }
        List<PmsBrand> list = brandService.listByIds(brandIds);
        
        // 转换为DTO
        List<PmsBrandDTO> dtoList = list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return Result.success(dtoList);
    }

    /**
     * 将Entity转换为DTO
     */
    private PmsBrandDTO convertToDTO(PmsBrand brand) {
        PmsBrandDTO dto = new PmsBrandDTO();
        BeanUtils.copyProperties(brand, dto);
        return dto;
    }
}

