package com.deko.mall.sms.controller.feign;

import com.deko.mall.common.result.Result;
import com.deko.mall.sms.api.dto.LiveRoomItemDTO;
import com.deko.mall.sms.model.vo.LiveRoomListVO;
import com.deko.mall.sms.service.LiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 直播内部 Feign 接口（供装修器直播组件自动取数）
 * <p>
 * 需在 mall-sms 的 security.whitelist-paths 放行 {@code /feign-api/v1/live/**}
 * （与 mall-cms 的 {@code /feign-api/v1/**} 口径一致），白名单在启动时构建，改动需重启。
 */
@Tag(name = "feign-直播内部接口")
@RestController
@RequestMapping("/feign-api/v1/live")
@RequiredArgsConstructor
public class LiveFeignController {

    private final LiveRoomService liveRoomService;

    @Operation(summary = "装修组件用直播间列表", description = "关键词/多状态/推荐筛选+条数，非分页，供装修器直播组件自动数据模式预览")
    @GetMapping("/diy-list")
    public Result<List<LiveRoomItemDTO>> getDiyRoomList(
            @Parameter(description = "关键词(标题/主播)") @RequestParam(required = false) String keywords,
            @Parameter(description = "状态筛选，多状态逗号分隔(如 0,1；空=全部)")
            @RequestParam(required = false) String status,
            @Parameter(description = "是否仅推荐(0-否;1-是；不传表示不筛选)")
            @RequestParam(required = false) Integer isRecommended,
            @Parameter(description = "返回条数") @RequestParam(required = false) Integer number) {
        List<LiveRoomListVO> list = liveRoomService.getDiyRoomList(keywords, status, number, isRecommended);
        return Result.success(list.stream().map(this::convert).toList());
    }

    private LiveRoomItemDTO convert(LiveRoomListVO vo) {
        LiveRoomItemDTO dto = new LiveRoomItemDTO();
        dto.setId(vo.getId());
        dto.setTitle(vo.getTitle());
        dto.setCover(vo.getCover());
        dto.setSellerName(vo.getSellerName());
        dto.setStatus(vo.getStatus());
        dto.setStatusName(vo.getStatusName());
        dto.setOnlineCount(vo.getOnlineCount());
        dto.setUrl(vo.getUrl());
        return dto;
    }
}
