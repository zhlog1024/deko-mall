package com.deko.mall.cms.api;

import com.deko.mall.cms.api.dto.VideoByIdsQuery;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 视频服务 Feign 客户端（供装修器 plugins-video-list/tabs 等消费）
 */
@FeignClient(value = "mall-cms", contextId = "cmsVideo")
public interface CmsVideoFeignClient {

    /**
     * 视频分页（装修器"自动数据/筛选"模式拉视频列表）
     *
     * @param query 分页查询
     * @return 视频数据分页
     */
    @PostMapping("/feign-api/v1/video/page")
    PageResult<VideoItemDTO> page(@RequestBody VideoFeignPageQuery query);

    /**
     * 指定视频数据（装修器"指定视频"模式：按 ids 回显）
     *
     * @param query 视频ID列表
     * @return 视频数据项
     */
    @PostMapping("/feign-api/v1/video/by-ids")
    Result<List<VideoItemDTO>> listByIds(@RequestBody VideoByIdsQuery query);
}
