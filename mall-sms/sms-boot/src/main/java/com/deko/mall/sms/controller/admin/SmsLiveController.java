package com.deko.mall.sms.controller.admin;

import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.sms.config.LiveProperties;
import com.deko.mall.sms.model.form.LiveRoomForm;
import com.deko.mall.sms.model.form.LiveRoomGoodsForm;
import com.deko.mall.sms.model.query.LiveRoomPageQuery;
import com.deko.mall.sms.model.vo.LiveRoomDetailVO;
import com.deko.mall.sms.model.vo.LiveRoomPageVO;
import com.deko.mall.sms.model.vo.LiveSettingsVO;
import com.deko.mall.sms.service.LiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Admin-直播管理
 */
@Tag(name = "Admin-直播管理")
@RestController
@RequestMapping("/api/v1/live")
@Slf4j
@RequiredArgsConstructor
public class SmsLiveController {

    private final LiveRoomService liveRoomService;
    private final LiveProperties liveProperties;

    @Operation(summary = "直播间分页列表")
    @GetMapping("/rooms/page")
    public PageResult<LiveRoomPageVO> getRoomPage(LiveRoomPageQuery queryParams) {
        return PageResult.success(liveRoomService.getRoomPage(queryParams));
    }

    @Operation(summary = "直播间详情（管理端，含推拉流地址）")
    @GetMapping("/rooms/{id}")
    public Result<LiveRoomPageVO> getRoomDetail(
            @Parameter(description = "直播间ID") @PathVariable Long id) {
        return Result.success(liveRoomService.getAdminDetail(id));
    }

    @Operation(summary = "直播间商品橱窗（管理端）")
    @GetMapping("/rooms/{id}/goods")
    public Result<List<com.deko.mall.sms.model.vo.LiveGoodsVO>> getRoomGoods(
            @Parameter(description = "直播间ID") @PathVariable Long id) {
        return Result.success(liveRoomService.getRoomGoods(id));
    }

    @Operation(summary = "新增直播间")
    @PostMapping("/rooms")
    public Result<Long> createRoom(@RequestBody LiveRoomForm form) {
        return Result.success(liveRoomService.createRoom(form));
    }

    @Operation(summary = "编辑直播间")
    @PutMapping("/rooms/{id}")
    public Result<Boolean> updateRoom(
            @Parameter(description = "直播间ID") @PathVariable Long id,
            @RequestBody LiveRoomForm form) {
        form.setId(id);
        liveRoomService.updateRoom(form);
        return Result.success(true);
    }

    @Operation(summary = "删除直播间（多个以英文逗号分隔）")
    @DeleteMapping("/rooms/{ids}")
    public Result<Boolean> deleteRooms(
            @Parameter(description = "直播间ID，多个以英文逗号(,)分割") @PathVariable("ids") String ids) {
        List<Long> idList = Arrays.stream(ids.split(",")).map(Long::valueOf).toList();
        liveRoomService.deleteRooms(idList);
        return Result.success(true);
    }

    @Operation(summary = "绑定直播商品")
    @PostMapping("/rooms/goods")
    public Result<Boolean> bindGoods(@RequestBody LiveRoomGoodsForm form) {
        liveRoomService.bindGoods(form);
        return Result.success(true);
    }

    @Operation(summary = "解绑直播商品")
    @DeleteMapping("/rooms/goods/{goodsId}")
    public Result<Boolean> unbindGoods(
            @Parameter(description = "直播商品ID") @PathVariable Long goodsId) {
        liveRoomService.unbindGoods(goodsId);
        return Result.success(true);
    }

    @Operation(summary = "设置讲解商品")
    @PutMapping("/rooms/goods/{goodsId}/explain")
    public Result<Boolean> setExplainGoods(
            @Parameter(description = "直播商品ID") @PathVariable Long goodsId) {
        liveRoomService.setExplainGoods(goodsId);
        return Result.success(true);
    }

    @Operation(summary = "变更直播状态（1开播/2离开/0下播/3封禁）")
    @PutMapping("/rooms/{id}/status")
    public Result<Boolean> changeStatus(
            @Parameter(description = "直播间ID") @PathVariable Long id,
            @Parameter(description = "状态(1-开播;2-离开;0-下播;3-封禁)")
            @RequestParam Integer status) {
        liveRoomService.changeStatus(id, status);
        return Result.success(true);
    }

    @Operation(summary = "重新生成推拉流地址")
    @PostMapping("/rooms/{id}/refresh-stream")
    public Result<LiveRoomPageVO> refreshStreamUrl(
            @Parameter(description = "直播间ID") @PathVariable Long id) {
        return Result.success(liveRoomService.refreshStreamUrl(id));
    }

    @Operation(summary = "全局直播设置（WS地址/腾讯云配置）")
    @GetMapping("/settings")
    public Result<LiveSettingsVO> getSettings() {
        // 不能直接返回 LiveProperties（@RefreshScope 代理，Jackson 序列化报类型错误）
        LiveSettingsVO vo = new LiveSettingsVO();
        vo.setTencentSecretId(liveProperties.getTencentSecretId());
        vo.setTencentSecretKey(liveProperties.getTencentSecretKey());
        vo.setPushDomain(liveProperties.getPushDomain());
        vo.setPlayDomain(liveProperties.getPlayDomain());
        vo.setPushAuthKey(liveProperties.getPushAuthKey());
        vo.setPlayUseHttps(liveProperties.getPlayUseHttps());
        vo.setWsHost(liveProperties.getWsHost());
        vo.setWsPort(liveProperties.getWsPort());
        vo.setWsIsWss(liveProperties.getWsIsWss());
        vo.setApplicationName(liveProperties.getApplicationName());
        return Result.success(vo);
    }
}
