package com.deko.mall.pms.controller.feign;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.dto.SpuDynamicDataDTO;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.SpuPageVO;
import com.deko.mall.pms.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * feign-商品控制层
 */
@Tag(name = "feign-商品SPU接口")
@RestController
@RequestMapping("/feign-api/v1/spu")
@AllArgsConstructor
@Slf4j
public class SpuFeignController implements SpuFeignClient {

    private SpuService spuService;

    @PostMapping("/get/spu-list")
    @Override
    public Result<List<PmsSpuDTO>> getSpuList(@RequestBody List<Long> productIds) {
        List<PmsSpuDTO> products = spuService.getProductList(productIds);
        return Result.success(products);
    }

    @PostMapping("/get/spu-list-app")
    @Override
    public Result<List<PmsSpuDTO>> getSpuListForApp(@RequestBody List<Long> productIds) {
        List<PmsSpuDTO> products = spuService.getProductListForApp(productIds);
        return Result.success(products);
    }

    /**
     * 获取SPU商品动态数据 - 专注于SPU维度
     * 新增接口，用于页面装修系统等只需要SPU数据的场景
     * 返回的key格式统一为"s{spuId}"，更加简洁
     *
     * @param spuIds SPU ID列表
     * @return 商品动态数据结果集，key格式为"s{spuId}"
     */
    @Operation(summary = "获取SPU商品动态数据", description = "获取SPU维度的商品动态数据(价格、库存、销量等)，专为页面装修系统优化")
    @GetMapping("/get/dynamic-data")
    @Override
    public Result<Map<String, SpuDynamicDataDTO>> getSpuDynamicData(
            @Parameter(description = "SPU ID列表", required = true) @RequestParam List<Long> spuIds) {

        log.info("获取SPU维度商品动态数据: spuIds={}", spuIds);

        if (spuIds == null || spuIds.isEmpty()) {
            return Result.success(new HashMap<>());
        }

        try {
            // 调用Service层方法获取SPU动态数据
            Map<String, SpuDynamicDataDTO> resultMap = spuService.getSpuDynamicDataMap(spuIds);
            return Result.success(resultMap);
        } catch (Exception e) {
            log.error("获取SPU动态数据失败", e);
            return Result.failed("获取商品动态数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取商品分页列表", description = "用于装修器选择商品")
    @PostMapping("/page")
    @Override
    public Result<Map<String, Object>> getSpuPage(@RequestBody Map<String, Object> params) {
        try {
            // 转换参数
            SpuPageQuery query = convertToSpuPageQuery(params);
            
            // 查询数据
            IPage<SpuPageVO> page = spuService.listPagedSpuForApp(query);
            
            // 转换为Map格式
            Map<String, Object> result = new HashMap<>();
            result.put("records", page.getRecords().stream()
                    .map(this::convertSpuToMap)
                    .collect(Collectors.toList()));
            result.put("total", page.getTotal());
            result.put("pages", page.getPages());
            result.put("current", page.getCurrent());
            result.put("size", page.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取商品分页列表失败", e);
            return Result.failed("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 转换参数为 SpuPageQuery
     */
    private SpuPageQuery convertToSpuPageQuery(Map<String, Object> params) {
        SpuPageQuery query = new SpuPageQuery();
        
        // 前端发送的是 page，不是 pageNum
        if (params.get("page") != null) {
            query.setPageNum(Integer.parseInt(params.get("page").toString()));
        }
        // 前端发送的是 page_size
        if (params.get("page_size") != null) {
            int pageSize = Integer.parseInt(params.get("page_size").toString());
            query.setPageSize(Math.min(pageSize, 500)); // 限制最大500
        }
        if (params.get("keywords") != null) {
            query.setKeywords(params.get("keywords").toString());
        }
        if (params.get("name") != null) {
            query.setName(params.get("name").toString());
        }
        // 前端发送的是 category_id
        if (params.get("category_id") != null) {
            query.setCategoryId(Long.parseLong(params.get("category_id").toString()));
        }
        // 前端发送的是 brand_ids
        if (params.get("brand_ids") != null) {
            query.setBrandId(Long.parseLong(params.get("brand_ids").toString()));
        }
        // 前端发送的是 category_level
        if (params.get("category_level") != null) {
            query.setCategoryLevel(Integer.parseInt(params.get("category_level").toString()));
        }
        // 前端发送的是 spu_type
        if (params.get("spu_type") != null) {
            query.setSpuType(params.get("spu_type").toString());
        }
        if (params.get("status") != null) {
            query.setStatus(Integer.parseInt(params.get("status").toString()));
        }
        if (params.get("sortField") != null) {
            query.setSortField(params.get("sortField").toString());
        }
        if (params.get("sort") != null) {
            query.setSort(params.get("sort").toString());
        }
        
        return query;
    }

    /**
     * 转换 SpuPageVO 为 Map
     */
    private Map<String, Object> convertSpuToMap(SpuPageVO spu) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", spu.getId());
        map.put("name", spu.getName());
        map.put("picUrl", spu.getPicUrl());
        map.put("price", spu.getPrice());
        map.put("originPrice", spu.getOriginPrice());
        map.put("sales", spu.getSales());
        map.put("spuType", spu.getSpuType());
        map.put("spuTypeDesc", spu.getSpuTypeDesc());
        map.put("categoryName", spu.getCategoryName());
        map.put("brandName", spu.getBrandName());
        map.put("simpleDesc", spu.getSimpleDesc()); // 添加商品简述字段
        return map;
    }

}
