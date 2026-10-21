package com.deko.mall.sms.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.sms.model.vo.LiveGoodsVO;
import com.deko.mall.sms.model.vo.LiveIndexInitVO;
import com.deko.mall.sms.model.vo.LiveRoomDetailVO;
import com.deko.mall.sms.model.vo.LiveRoomListDataVO;
import com.deko.mall.sms.model.vo.LiveSearchRecordVO;
import com.deko.mall.sms.service.LiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * App-直播
 */
@Tag(name = "App-直播")
@RestController
@RequestMapping("/app-api/v1/live")
@Slf4j
@RequiredArgsConstructor
public class LiveAppController {

    private final LiveRoomService liveRoomService;

    @Operation(summary = "直播首页初始化数据（状态Tabs/轮播/基础信息）")
    @GetMapping("/rooms/tabs")
    public Result<LiveIndexInitVO> getInitData() {
        return Result.success(liveRoomService.getInitData());
    }

    @Operation(summary = "直播间列表（按状态筛选）")
    @GetMapping("/rooms")
    public Result<LiveRoomListDataVO> getRoomList(
            @Parameter(description = "状态(空-全部;0-未开播;1-直播中;2-离开)")
            @RequestParam(required = false) String status,
            @Parameter(description = "关键词(标题/主播)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页条数") @RequestParam(required = false) Integer pageSize) {
        return Result.success(liveRoomService.getRoomList(status, keyword, null, page, pageSize));
    }

    @Operation(summary = "直播间详情")
    @GetMapping("/rooms/{id}")
    public Result<LiveRoomDetailVO> getRoomDetail(
            @Parameter(description = "直播间ID") @PathVariable Long id) {
        return Result.success(liveRoomService.getRoomDetail(id));
    }

    @Operation(summary = "直播间商品橱窗")
    @GetMapping("/rooms/{id}/goods")
    public Result<List<LiveGoodsVO>> getRoomGoods(
            @Parameter(description = "直播间ID") @PathVariable Long id) {
        return Result.success(liveRoomService.getRoomGoods(id));
    }

    @Operation(summary = "直播搜索初始化数据")
    @GetMapping("/search/tabs")
    public Result<LiveIndexInitVO> getSearchInitData() {
        return Result.success(liveRoomService.getInitData());
    }

    @Operation(summary = "装修首页直播列表（diy autolivelist，返回数组）")
    @GetMapping("/diy-list")
    public Result<List<com.deko.mall.sms.model.vo.LiveRoomListVO>> getDiyRoomList(
            @Parameter(description = "关键词(标题/主播)") @RequestParam(required = false) String keywords,
            @Parameter(description = "状态筛选，多状态逗号分隔(如 0,1;空-全部;0-未开播;1-直播中;2-离开)")
            @RequestParam(required = false) String status,
            @Parameter(description = "是否仅推荐(0-否;1-是；不传表示不筛选)")
            @RequestParam(required = false) Integer isRecommended,
            @Parameter(description = "返回条数") @RequestParam(required = false) Integer number) {
        return Result.success(liveRoomService.getDiyRoomList(keywords, status, number, isRecommended));
    }

    @Operation(summary = "直播搜索记录页数据（热门搜索）")
    @GetMapping("/rooms/hot-search")
    public Result<LiveSearchRecordVO> getSearchRecordData() {
        return Result.success(liveRoomService.getSearchRecordData());
    }

    @Operation(summary = "直播搜索列表")
    @GetMapping("/rooms/search")
    public Result<LiveRoomListDataVO> searchRooms(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态(空-全部;0-未开播;1-直播中;2-离开)")
            @RequestParam(required = false) String status,
            @Parameter(description = "排序依据(default-默认;hot-人气;new-最新)")
            @RequestParam(required = false) String by,
            @Parameter(description = "页码") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页条数") @RequestParam(required = false) Integer pageSize) {
        return Result.success(liveRoomService.getRoomList(status, keyword, by, page, pageSize));
    }
}
