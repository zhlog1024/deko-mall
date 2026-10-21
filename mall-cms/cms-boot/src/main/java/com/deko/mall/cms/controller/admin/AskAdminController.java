package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.AskEnableDTO;
import com.deko.mall.cms.model.dto.AskReplyDTO;
import com.deko.mall.cms.model.dto.AskSaveDTO;
import com.deko.mall.cms.model.query.AskPageQuery;
import com.deko.mall.cms.model.vo.AskDetailVO;
import com.deko.mall.cms.model.vo.AskVO;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 管理端-商品问答接口
 */
@Tag(name = "admin-商品问答管理接口")
@RestController
@RequestMapping("/api/v1/ask")
@RequiredArgsConstructor
public class AskAdminController {

    private final AskService askService;

    @Operation(summary = "问答分页列表", description = "keyword/spuId/isReply/isEnable 过滤，按 sort/create_time 排序")
    @GetMapping("/page")
    public PageResult<AskVO> pageAsks(AskPageQuery query) {
        IPage<AskVO> result = askService.pageAsks(query);
        return PageResult.success(result);
    }

    @Operation(summary = "问答详情(编辑回显,含问题与回答)")
    @GetMapping("/{id}")
    public Result<AskDetailVO> getDetail(@Parameter(description = "问答ID") @PathVariable Long id) {
        return Result.success(askService.getDetail(id));
    }

    @Operation(summary = "新增问答(运营代录问题)")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('cms:ask:add')")
    public Result<Boolean> create(@Valid @RequestBody AskSaveDTO dto) {
        return Result.judge(askService.createAsk(dto));
    }

    @Operation(summary = "编辑问答", description = "标题/内容/商品/排序/启用可改；不改 answer/is_reply")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:ask:edit')")
    public Result<Boolean> update(@Parameter(description = "问答ID") @PathVariable Long id,
                                  @RequestBody AskSaveDTO dto) {
        dto.setId(id);
        return Result.judge(askService.updateAsk(dto));
    }

    @Operation(summary = "回复问答", description = "置 answer/is_reply=1/replyUser/replyTime，同时自动启用展示")
    @PutMapping("/{id}/reply")
    @PreAuthorize("@ss.hasPerm('cms:ask:reply')")
    public Result<Boolean> reply(@Parameter(description = "问答ID") @PathVariable Long id,
                                 @Valid @RequestBody AskReplyDTO dto) {
        return Result.judge(askService.replyAsk(id, dto));
    }

    @Operation(summary = "确认/停用问答", description = "对用户提问 0→1 的确认展示操作")
    @PutMapping("/{id}/enable")
    @PreAuthorize("@ss.hasPerm('cms:ask:enable')")
    public Result<Boolean> enable(@Parameter(description = "问答ID") @PathVariable Long id,
                                  @Valid @RequestBody AskEnableDTO dto) {
        return Result.judge(askService.enableAsk(id, dto));
    }

    @Operation(summary = "删除问答(逻辑删除)")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('cms:ask:delete')")
    public Result<Boolean> delete(@Parameter(description = "问答ID，多个以英文逗号(,)分割") @PathVariable("ids") String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .toList();
        return Result.judge(askService.deleteAsks(idList));
    }
}
