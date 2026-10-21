package com.deko.mall.cms.controller.feign;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.api.dto.VideoByIdsQuery;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
import com.deko.mall.cms.service.VideoService;
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
 * 视频内部 Feign 接口（供装修器 plugins-video-list/tabs 取数/回显等）
 */
@Tag(name = "feign-视频内部接口")
@RestController
@RequestMapping("/feign-api/v1/video")
@RequiredArgsConstructor
public class VideoFeignController {

    private final VideoService videoService;

    @PostMapping("/page")
    public PageResult<VideoItemDTO> page(@RequestBody VideoFeignPageQuery query) {
        IPage<VideoItemDTO> result = videoService.pageFeign(query);
        return PageResult.success(result);
    }

    @PostMapping("/by-ids")
    public Result<List<VideoItemDTO>> listByIds(@RequestBody VideoByIdsQuery query) {
        return Result.success(videoService.listByIdsFeign(query == null ? null : query.getIds()));
    }
}
