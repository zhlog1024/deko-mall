package com.deko.mall.cms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.AskAppAskDTO;
import com.deko.mall.cms.model.query.AskAppQuery;
import com.deko.mall.cms.model.vo.AskAppDetailVO;
import com.deko.mall.cms.model.vo.AskAppItemVO;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.web.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP端-商品问答接口
 */
@Tag(name = "App-商品问答接口", description = "问答列表/详情/用户提问")
@RestController
@RequestMapping("/app-api/v1/ask")
@RequiredArgsConstructor
public class AskAppController {

    private final AskService askService;

    @Operation(summary = "问答分页列表", description = "仅启用未删；spuId 空=全部启用问答")
    @GetMapping("/page")
    public PageResult<AskAppItemVO> page(AskAppQuery query) {
        IPage<AskAppItemVO> result = askService.appPage(query);
        return PageResult.success(result);
    }

    @Operation(summary = "问答详情", description = "访问量+1；未启用仅提问人本人可见，其余视为不存在")
    @GetMapping("/{id}")
    public Result<AskAppDetailVO> detail(@Parameter(description = "问答ID") @PathVariable Long id) {
        // 游客可读启用问答；登录态传入用于"提问人查看待确认问题"的归属校验
        Long memberId = SecurityUtils.getMemberId();
        return Result.success(askService.appDetail(id, memberId));
    }

    @Operation(summary = "用户提问", description = "登录会员对商品提问，提交后待运营确认展示(is_enable=0)")
    @PostMapping
    public Result<Long> ask(@Valid @RequestBody AskAppAskDTO dto) {
        Long memberId = SecurityUtils.getMemberId();
        if (memberId == null) {
            throw new BizException("请先登录后再提问");
        }
        return Result.success(askService.askByUser(dto, memberId));
    }
}
