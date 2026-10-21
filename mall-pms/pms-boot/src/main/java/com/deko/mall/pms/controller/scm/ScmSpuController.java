package com.deko.mall.pms.controller.scm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pms.model.form.PmsSpuForm;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.PmsSpuDetailVO;
import com.deko.mall.pms.model.vo.PmsSpuPageVO;
import com.deko.mall.pms.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-商品控制层
 */
@Tag(name = "Admin-商品SPU接口")
@RestController
@RequestMapping("/scm-api/v1/spu")
@AllArgsConstructor
public class ScmSpuController {

    private SpuService spuService;

    @Operation(summary = "商品分页列表")
    @GetMapping("/page")
    public PageResult listPagedSpu(SpuPageQuery queryParams) {
        IPage<PmsSpuPageVO> result = spuService.listPagedSpu(queryParams);
        return PageResult.success(result);
    }


    @Operation(summary = "商品详情")
    @GetMapping("/{id}/detail")
    public Result detail(@Parameter(name = "商品ID") @PathVariable Long id) {
        PmsSpuDetailVO pmsSpuDetailVO = spuService.getSpuDetail(id);
        return Result.success(pmsSpuDetailVO);
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result addSpu(@RequestBody PmsSpuForm formData) {
        boolean result = spuService.addSpu(formData);
        return Result.judge(result);
    }

    @Operation(summary = "修改商品")
    @PutMapping(value = "/{id}")
    public Result updateSpuById(
            @Parameter(name = "商品ID") @PathVariable Long id,
            @RequestBody PmsSpuForm formData
    ) {
        boolean result = spuService.updateSpuById(id, formData);
        return Result.judge(result);
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{ids}")
    public Result delete(
            @Parameter(name = "商品ID,多个以英文逗号(,)分隔") @PathVariable String ids
    ) {
        boolean result = spuService.removeBySpuIds(ids);
        return Result.judge(result);
    }

    @Operation(summary = "商品上架")
    @PutMapping("/{id}/publish")
    public Result<Boolean> publishSpu(@Parameter(name = "商品ID") @PathVariable Long id) {
        boolean result = spuService.publishSpu(id);
        return Result.judge(result);
    }

    @Operation(summary = "商品下架")
    @PutMapping("/{id}/unpublish")
    public Result<Boolean> unpublishSpu(@Parameter(name = "商品ID") @PathVariable Long id) {
        boolean result = spuService.unpublishSpu(id);
        return Result.judge(result);
    }

    @Operation(summary = "商品批量上架")
    @PutMapping("/batch/publish")
    public Result<Boolean> batchPublishSpu(@RequestBody List<Long> ids) {
        boolean result = spuService.batchPublishSpu(ids);
        return Result.judge(result);
    }

    @Operation(summary = "商品批量下架")
    @PutMapping("/batch/unpublish")
    public Result<Boolean> batchUnpublishSpu(@RequestBody List<Long> ids) {
        boolean result = spuService.batchUnpublishSpu(ids);
        return Result.judge(result);
    }
}
