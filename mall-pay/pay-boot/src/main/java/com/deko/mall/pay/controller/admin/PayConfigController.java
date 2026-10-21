package com.deko.mall.pay.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.pay.config.PayConfigManager;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.model.form.PayConfigForm;
import com.deko.mall.pay.model.query.PayConfigQuery;
import com.deko.mall.pay.model.vo.PayChannelVO;
import com.deko.mall.pay.model.vo.PayConfigVO;
import com.deko.mall.pay.service.pay.PayConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付配置管理控制器
 */
@Tag(name = "01.支付配置管理")
@RestController
@RequestMapping("/api/v1/pay/config")
@RequiredArgsConstructor
public class PayConfigController {

    private final PayConfigService payConfigService;

    private final PayConfigManager payConfigManager;

    @Operation(summary = "获取支付配置分页列表")
    @GetMapping("/page")
    public PageResult<PayConfigVO> getPayConfigPage(
            PayConfigQuery queryParams
    ) {
        Page<PayConfigVO> page = payConfigService.getPayConfigPage(queryParams);
        return PageResult.success(page);
    }

    @Operation(summary = "获取支付配置详情")
    @GetMapping("/{id}")
    public Result<PayConfigVO> getPayConfigDetail(
            @Parameter(description = "配置ID") @PathVariable Long id
    ) {
        return Result.success(payConfigService.getPayConfigDetail(id));
    }

    @Operation(summary = "新增支付配置")
    @PostMapping()
    public Result<Boolean> addPayConfig(
            @Valid @RequestBody PayConfigForm form
    ) {
        PayConfig payConfig = payConfigService.addPayConfig(form);
        return Result.success();
    }

    @Operation(summary = "修改支付配置")
    @PutMapping("/{id}")
    public Result<Boolean> updatePayConfig(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Valid @RequestBody PayConfigForm form
    ) {

        PayConfig payConfig = payConfigService.updatePayConfig(id, form);

        payConfigManager.reloadConfig(payConfig.getPayChannel());

        return Result.success();
    }

    @Operation(summary = "删除支付配置")
    @DeleteMapping("/{ids}")
    public Result<Boolean> deletePayConfigs(
            @Parameter(description = "配置ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return Result.judge(payConfigService.deletePayConfigs(idList));
    }

    @Operation(summary = "获取支付渠道列表")
    @GetMapping("/channels")
    public Result<List<PayChannelVO>> getPayChannels() {
        return Result.success(payConfigService.getPayChannels());
    }

    @Operation(summary = "获取轮询策略状态")
    @GetMapping("/polling-status")
    public Result<Boolean> getPollingStatus() {
        return Result.success(payConfigManager.isPollingEnabled());
    }

    @Operation(summary = "设置轮询策略状态")
    @PutMapping("/polling-status/{enabled}")
    public Result<Boolean> setPollingStatus(
            @Parameter(description = "是否启用轮询策略") @PathVariable Boolean enabled
    ) {
        payConfigManager.setPollingEnabled(enabled);
        return Result.success(true);
    }
}
