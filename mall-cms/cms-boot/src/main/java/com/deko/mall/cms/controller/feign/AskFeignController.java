package com.deko.mall.cms.controller.feign;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.api.dto.AskByIdsQuery;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品问答内部 Feign 接口（供装修器选问答/回显等）
 */
@Tag(name = "feign-商品问答内部接口")
@RestController
@RequestMapping("/feign-api/v1/ask")
@RequiredArgsConstructor
public class AskFeignController {

    private final AskService askService;

    @PostMapping("/page")
    public PageResult<AskItemDTO> page(@RequestBody AskFeignPageQuery query) {
        IPage<AskItemDTO> result = askService.pageFeign(query);
        return PageResult.success(result);
    }

    @PostMapping("/by-ids")
    public Result<List<AskItemDTO>> listByIds(@RequestBody AskByIdsQuery query) {
        return Result.success(askService.listByIdsFeign(query == null ? null : query.getIds()));
    }
}
