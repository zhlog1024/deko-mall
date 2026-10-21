package com.deko.mall.cms.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.query.VideoAppQuery;
import com.deko.mall.cms.model.vo.VideoAppDetailVO;
import com.deko.mall.cms.model.vo.VideoAppItemVO;
import com.deko.mall.cms.service.VideoService;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP端-视频接口
 */
@Tag(name = "App-视频接口", description = "视频列表/详情(播放)")
@RestController
@RequestMapping("/app-api/v1/video")
@RequiredArgsConstructor
public class VideoAppController {

    private final VideoService videoService;

    @Operation(summary = "视频分页列表", description = "仅启用未删；keyword/isRecommended/isHot 过滤")
    @GetMapping("/page")
    public PageResult<VideoAppItemVO> page(VideoAppQuery query) {
        IPage<VideoAppItemVO> result = videoService.appPage(query);
        return PageResult.success(result);
    }

    @Operation(summary = "视频详情", description = "访问量+1；未启用或已删除视为不存在")
    @GetMapping("/{id}")
    public Result<VideoAppDetailVO> detail(@Parameter(description = "视频ID") @PathVariable Long id) {
        return Result.success(videoService.appDetail(id));
    }
}
