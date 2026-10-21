package com.deko.mall.pms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.pms.model.query.PmsSpuGroupQuery;
import com.deko.mall.pms.model.vo.PmsSpuRenderVO;
import com.deko.mall.pms.model.vo.SpuPageVO;
import com.deko.mall.pms.service.PmsSpuGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP端商品分组接口
 * 提供APP端直接调用的商品分组相关接口
 */
@Tag(name = "APP端商品分组接口")
@RestController
@RequestMapping("/app-api/v1/spu-groups")
@RequiredArgsConstructor
public class PmsSpuGroupAppController {

    private final PmsSpuGroupService spuGroupService;

    @Operation(summary = "分页获取分组商品", description = "根据分组ID分页获取分组内的商品列表，包含价格、库存、销量等信息")
    @PostMapping("/page")
    public PageResult<PmsSpuRenderVO> pageGroupSpu(@RequestBody PmsSpuGroupQuery query) {
        IPage<PmsSpuRenderVO> page = spuGroupService.pageGroupSpu(query);
        return PageResult.success(page);
    }

    @Operation(summary = "分页获取分组商品（SpuPageVO格式）", description = "用于装修组件场景，返回标准 SpuPageVO 格式的商品数据")
    @PostMapping("/page/spu-page-vo")
    public PageResult<SpuPageVO> pageGroupSpuForSpuPageVO(@RequestBody PmsSpuGroupQuery query) {
        IPage<SpuPageVO> page = spuGroupService.pageGroupSpuForSpuPageVO(query);
        return PageResult.success(page);
    }
}
