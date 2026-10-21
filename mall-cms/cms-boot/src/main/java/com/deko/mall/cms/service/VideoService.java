package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
import com.deko.mall.cms.model.dto.VideoEnableDTO;
import com.deko.mall.cms.model.dto.VideoSaveDTO;
import com.deko.mall.cms.model.entity.Video;
import com.deko.mall.cms.model.query.VideoAppQuery;
import com.deko.mall.cms.model.query.VideoPageQuery;
import com.deko.mall.cms.model.vo.VideoAppDetailVO;
import com.deko.mall.cms.model.vo.VideoAppItemVO;
import com.deko.mall.cms.model.vo.VideoDetailVO;
import com.deko.mall.cms.model.vo.VideoVO;

import java.util.List;

/**
 * 视频服务
 */
public interface VideoService extends IService<Video> {

    /**
     * 管理端分页列表（含禁用，keyword/isRecommended/isHot/isEnable 过滤，排序见 VideoPageQuery.sortField）
     */
    IPage<VideoVO> pageVideos(VideoPageQuery query);

    /**
     * 管理端详情（编辑回显，含 videoUrl）
     */
    VideoDetailVO getDetail(Long id);

    /**
     * 管理端新增（运营上传 PGC 视频，默认启用）
     */
    boolean createVideo(VideoSaveDTO dto);

    /**
     * 管理端编辑（标题/封面/视频地址/简介/推荐/热度/排序/启用可改）
     */
    boolean updateVideo(VideoSaveDTO dto);

    /**
     * 运营启用/停用（is_enable 1/0）
     */
    boolean enableVideo(Long id, VideoEnableDTO dto);

    /**
     * 管理端批量删除（逻辑删除）
     */
    boolean deleteVideos(List<Long> ids);

    /**
     * App 端分页列表（仅启用未删，keyword/isRecommended/isHot 过滤，排序见 VideoAppQuery.sortField）
     */
    IPage<VideoAppItemVO> appPage(VideoAppQuery query);

    /**
     * App 端详情（访问量+1；未启用或已删除视为不存在）
     */
    VideoAppDetailVO appDetail(Long id);

    /**
     * Feign：分页视频（装修器 plugins-video-list/tabs 取数，默认仅启用）
     */
    IPage<VideoItemDTO> pageFeign(VideoFeignPageQuery query);

    /**
     * Feign：按 ids 回显（仅启用未删）
     */
    List<VideoItemDTO> listByIdsFeign(List<Long> ids);
}
