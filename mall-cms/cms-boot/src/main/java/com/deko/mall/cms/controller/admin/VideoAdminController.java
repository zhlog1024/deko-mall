package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.VideoEnableDTO;
import com.deko.mall.cms.model.dto.VideoSaveDTO;
import com.deko.mall.cms.model.query.VideoPageQuery;
import com.deko.mall.cms.model.vo.VideoDetailVO;
import com.deko.mall.cms.model.vo.VideoVO;
import com.deko.mall.cms.service.VideoService;
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
 * 管理端-视频接口
 */
@Tag(name = "admin-视频管理接口")
@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoAdminController {

    private final VideoService videoService;

    @Operation(summary = "视频分页列表", description = "keyword/isRecommended/isHot/isEnable 过滤，按 sortField 排序")
    @GetMapping("/page")
    public PageResult<VideoVO> pageVideos(VideoPageQuery query) {
        IPage<VideoVO> result = videoService.pageVideos(query);
        return PageResult.success(result);
    }

    @Operation(summary = "视频详情(编辑回显,含视频地址)")
    @GetMapping("/{id}")
    public Result<VideoDetailVO> getDetail(@Parameter(description = "视频ID") @PathVariable Long id) {
        return Result.success(videoService.getDetail(id));
    }

    @Operation(summary = "新增视频(运营上传 PGC 视频)")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('cms:video:add')")
    public Result<Boolean> create(@Valid @RequestBody VideoSaveDTO dto) {
        return Result.judge(videoService.createVideo(dto));
    }

    @Operation(summary = "编辑视频", description = "标题/封面/视频地址/简介/推荐/热度/排序/启用可改")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('cms:video:edit')")
    public Result<Boolean> update(@Parameter(description = "视频ID") @PathVariable Long id,
                                  @RequestBody VideoSaveDTO dto) {
        dto.setId(id);
        return Result.judge(videoService.updateVideo(dto));
    }

    @Operation(summary = "启用/停用视频")
    @PutMapping("/{id}/enable")
    @PreAuthorize("@ss.hasPerm('cms:video:enable')")
    public Result<Boolean> enable(@Parameter(description = "视频ID") @PathVariable Long id,
                                  @Valid @RequestBody VideoEnableDTO dto) {
        return Result.judge(videoService.enableVideo(id, dto));
    }

    @Operation(summary = "删除视频(逻辑删除)")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('cms:video:delete')")
    public Result<Boolean> delete(@Parameter(description = "视频ID，多个以英文逗号(,)分割") @PathVariable("ids") String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .toList();
        return Result.judge(videoService.deleteVideos(idList));
    }
}
