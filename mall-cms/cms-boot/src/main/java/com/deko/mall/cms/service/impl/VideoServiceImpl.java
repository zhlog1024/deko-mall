package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
import com.deko.mall.cms.mapper.VideoMapper;
import com.deko.mall.cms.model.dto.VideoEnableDTO;
import com.deko.mall.cms.model.dto.VideoSaveDTO;
import com.deko.mall.cms.model.entity.Video;
import com.deko.mall.cms.model.query.VideoAppQuery;
import com.deko.mall.cms.model.query.VideoPageQuery;
import com.deko.mall.cms.model.vo.VideoAppDetailVO;
import com.deko.mall.cms.model.vo.VideoAppItemVO;
import com.deko.mall.cms.model.vo.VideoDetailVO;
import com.deko.mall.cms.model.vo.VideoVO;
import com.deko.mall.cms.service.VideoService;
import com.deko.mall.common.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频服务实现
 */
@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Override
    public IPage<VideoVO> pageVideos(VideoPageQuery query) {
        Page<Video> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Video> wrapper = buildBaseWrapper();
        applyCommonFilters(wrapper, query.getKeyword(), query.getIsRecommended(), query.getIsHot());
        if (query.getIsEnable() != null) {
            wrapper.eq(Video::getIsEnable, query.getIsEnable());
        }
        applyOrder(wrapper, query.getSortField(), null);

        IPage<Video> result = page(page, wrapper);
        List<VideoVO> voList = result.getRecords().stream().map(this::toAdminVO).toList();

        Page<VideoVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public VideoDetailVO getDetail(Long id) {
        Video video = requireVideo(id, false);
        VideoDetailVO vo = new VideoDetailVO();
        BeanUtils.copyProperties(video, vo);
        return vo;
    }

    @Override
    public boolean createVideo(VideoSaveDTO dto) {
        Video video = new Video();
        BeanUtils.copyProperties(dto, video);
        if (video.getDuration() == null) {
            video.setDuration(0);
        }
        if (video.getIsRecommended() == null) {
            video.setIsRecommended(0);
        }
        if (video.getIsHot() == null) {
            video.setIsHot(0);
        }
        if (video.getSort() == null) {
            video.setSort(0);
        }
        if (video.getIsEnable() == null) {
            video.setIsEnable(1);
        }
        video.setAccessCount(0);
        video.setDeleted(0);
        return save(video);
    }

    @Override
    public boolean updateVideo(VideoSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("视频ID不能为空");
        }
        requireVideo(dto.getId(), false);
        Video video = new Video();
        BeanUtils.copyProperties(dto, video);
        return updateById(video);
    }

    @Override
    public boolean enableVideo(Long id, VideoEnableDTO dto) {
        requireVideo(id, false);
        Video video = new Video();
        video.setId(id);
        video.setIsEnable(dto.getIsEnable());
        return updateById(video);
    }

    @Override
    public boolean deleteVideos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        LambdaUpdateWrapper<Video> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Video::getId, ids).set(Video::getDeleted, 1);
        return update(wrapper);
    }

    @Override
    public IPage<VideoAppItemVO> appPage(VideoAppQuery query) {
        Page<Video> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Video> wrapper = buildBaseWrapper();
        wrapper.eq(Video::getIsEnable, 1);
        applyCommonFilters(wrapper, query.getKeyword(), query.getIsRecommended(), query.getIsHot());
        applyOrder(wrapper, query.getSortField(), query.getOrderByRule());

        IPage<Video> result = page(page, wrapper);
        List<VideoAppItemVO> voList = result.getRecords().stream().map(this::toAppItemVO).toList();

        Page<VideoAppItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public VideoAppDetailVO appDetail(Long id) {
        Video video = requireVideo(id, true);
        // 访问量自增（SQL 原子更新，不触发字段自动填充）
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).setSql("access_count = access_count + 1");
        update(updateWrapper);

        VideoAppDetailVO vo = new VideoAppDetailVO();
        BeanUtils.copyProperties(video, vo);
        return vo;
    }

    @Override
    public IPage<VideoItemDTO> pageFeign(VideoFeignPageQuery query) {
        Page<Video> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Video> wrapper = buildBaseWrapper();
        Integer isEnable = query.getIsEnable() == null ? 1 : query.getIsEnable();
        wrapper.eq(Video::getIsEnable, isEnable);
        applyCommonFilters(wrapper, query.getKeyword(), query.getIsRecommended(), query.getIsHot());
        applyOrder(wrapper, query.getSortField(), query.getOrderByRule());

        IPage<Video> result = page(page, wrapper);
        List<VideoItemDTO> dtoList = result.getRecords().stream().map(this::toFeignItem).toList();

        Page<VideoItemDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public List<VideoItemDTO> listByIdsFeign(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Video> wrapper = buildBaseWrapper();
        wrapper.in(Video::getId, ids).eq(Video::getIsEnable, 1);
        return list(wrapper).stream().map(this::toFeignItem).toList();
    }

    // 私有工具

    private LambdaQueryWrapper<Video> buildBaseWrapper() {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getDeleted, 0);
        return wrapper;
    }

    /**
     * keyword(标题模糊)/isRecommended/isHot 公共过滤（admin/app/feign 共用）
     */
    private void applyCommonFilters(LambdaQueryWrapper<Video> wrapper, String keyword, Integer isRecommended, Integer isHot) {
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Video::getTitle, keyword);
        }
        if (isRecommended != null) {
            wrapper.eq(Video::getIsRecommended, isRecommended);
        }
        if (isHot != null) {
            wrapper.eq(Video::getIsHot, isHot);
        }
    }

    /**
     * 排序：createTime=创建时间/accessCount=浏览数/duration=时长 单列排序，方向由 orderByRule 决定
     * （0=降序默认/1=升序，DIY 编辑器规则 0降/1升）；综合(sortField 空或 sort)固定
     * sort asc + create_time desc，不受 orderByRule 影响。管理端不传 rule 保持倒序。
     */
    private void applyOrder(LambdaQueryWrapper<Video> wrapper, String sortField, Integer orderByRule) {
        boolean asc = orderByRule != null && orderByRule == 1;
        if ("createTime".equals(sortField)) {
            wrapper.orderBy(true, asc, Video::getCreateTime);
            return;
        }
        if ("accessCount".equals(sortField)) {
            wrapper.orderBy(true, asc, Video::getAccessCount);
            return;
        }
        if ("duration".equals(sortField)) {
            wrapper.orderBy(true, asc, Video::getDuration);
            return;
        }
        wrapper.orderByAsc(Video::getSort).orderByDesc(Video::getCreateTime);
    }

    /**
     * @param enabledOnly true=App 端可见性（未启用视为不存在），false=管理端（含禁用）
     */
    private Video requireVideo(Long id, boolean enabledOnly) {
        Video video = getById(id);
        if (video == null || video.getDeleted() != null && video.getDeleted() == 1) {
            throw new BizException("视频不存在");
        }
        if (enabledOnly && (video.getIsEnable() == null || video.getIsEnable() != 1)) {
            throw new BizException("视频已下架");
        }
        return video;
    }

    private VideoVO toAdminVO(Video video) {
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(video, vo);
        return vo;
    }

    private VideoAppItemVO toAppItemVO(Video video) {
        VideoAppItemVO vo = new VideoAppItemVO();
        BeanUtils.copyProperties(video, vo);
        return vo;
    }

    private VideoItemDTO toFeignItem(Video video) {
        VideoItemDTO dto = new VideoItemDTO();
        BeanUtils.copyProperties(video, dto);
        return dto;
    }
}
