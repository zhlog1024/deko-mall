package com.deko.mall.sms.api;

import com.deko.mall.common.result.Result;
import com.deko.mall.sms.api.dto.LiveRoomItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 直播 Feign 客户端（装修器直播组件自动取数数据源）
 * <p>
 * 消费方：装修器 {@code /api/v1/diy-designer/live/auto-list}。
 * 提供方：mall-sms {@code /feign-api/v1/live/diy-list}（需在 mall-sms 白名单放行）。
 */
@FeignClient(value = "mall-sms", contextId = "liveRoomFeignClient")
public interface LiveRoomFeignClient {

    /**
     * 装修组件用直播间列表（非分页，按 sort 升序取前 N 条）
     *
     * @param keywords      关键词(标题/主播，可空)
     * @param status        状态筛选，多状态用逗号分隔（如 "0,1"；空=全部，封禁状态不返回）
     * @param isRecommended 是否仅推荐(0-否;1-是；null=不筛选)
     * @param number        返回条数（默认 10，上限 50）
     * @return 直播间列表（snake_case 字段，与 app 端渲染契约一致）
     */
    @GetMapping("/feign-api/v1/live/diy-list")
    Result<List<LiveRoomItemDTO>> diyList(@RequestParam("keywords") String keywords,
                                          @RequestParam("status") String status,
                                          @RequestParam("isRecommended") Integer isRecommended,
                                          @RequestParam("number") Integer number);
}
