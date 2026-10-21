package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.deko.mall.common.web.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 视频服务单元测试（mock mapper，覆盖关键分支）
 * <p>
 * 覆盖：admin/app/feign 分页过滤分支(isRecommended/isHot/keyword/isEnable)、
 * sortField 各排序分支、详情访问量自增、启用停用、逻辑删除。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VideoServiceImplTest {

    private VideoServiceImpl videoService;

    @Mock
    private VideoMapper videoMapper;

    @BeforeEach
    void setUp() {
        // 纯单元测试初始化 MyBatis-Plus lambda 缓存（否则 Video::getXxx 方法引用报错）
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(
                        new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""),
                Video.class);
        videoService = Mockito.spy(new VideoServiceImpl());
        // 桩：写库方法（单测边界，真实 DB 交互不在此验证）
        doReturn(true).when(videoService).save(any());
        doReturn(true).when(videoService).updateById(any());
        doReturn(true).when(videoService).update(any(Wrapper.class));
        // 桩：getById 兜底（各测试按需覆盖）
        doReturn(null).when(videoService).getById(any());
    }

    private Video buildVideo(Long id) {
        Video video = new Video();
        video.setId(id);
        video.setTitle("养生操教学");
        video.setCover("https://cdn.example.com/video1.png");
        video.setVideoUrl("https://cdn.example.com/video1.mp4");
        video.setDuration(90);
        video.setDescription("跟着练，轻松养生");
        video.setAccessCount(66);
        video.setIsRecommended(1);
        video.setIsHot(1);
        video.setSort(0);
        video.setIsEnable(1);
        video.setDeleted(0);
        video.setCreateTime(LocalDateTime.of(2026, 8, 20, 9, 0));
        return video;
    }

    private String whereSql(AbstractWrapper wrapper) {
        return wrapper.getExpression().getNormal().getSqlSegment();
    }

    /**
     * 触发整体合并后取完整 SQL 段（含 ORDER BY），用于排序断言
     */
    private String fullSql(AbstractWrapper wrapper) {
        return wrapper.getSqlSegment();
    }

    // 管理端分页

    @Test
    @DisplayName("管理端分页：keyword/isRecommended/isHot/isEnable 过滤组合生效")
    void pageVideos_allFiltersApplied() {
        VideoPageQuery query = new VideoPageQuery();
        query.setKeyword("养生");
        query.setIsRecommended(1);
        query.setIsHot(1);
        query.setIsEnable(1);

        Video video = buildVideo(1L);
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of(video));
        page.setTotal(1);
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        IPage<VideoVO> result = videoService.pageVideos(query);

        assertEquals(1, result.getTotal());
        assertEquals("养生操教学", result.getRecords().get(0).getTitle());

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        String sql = whereSql(wrapper);
        assertTrue(sql.contains("deleted = "), "应过滤未删除: " + sql);
        assertTrue(sql.contains("title"), "应按标题模糊: " + sql);
        assertTrue(sql.contains("is_recommended"), "应按推荐过滤: " + sql);
        assertTrue(sql.contains("is_hot"), "应按热门过滤: " + sql);
        assertTrue(sql.contains("is_enable"), "应按启用状态过滤: " + sql);
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1), "1 值应入参");
    }

    @Test
    @DisplayName("管理端分页：无过滤条件仅未删除，默认自定义排序优先")
    void pageVideos_noFilterOnlyDeletedAndDefaultOrder() {
        VideoPageQuery query = new VideoPageQuery();
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.pageVideos(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        String sql = whereSql(wrapper);
        assertTrue(sql.contains("deleted = "), "应过滤未删除: " + sql);
        assertFalse(sql.contains("title"), "无关键字不应出现 like: " + sql);
        // 默认排序：sort asc + create_time desc 两列
        assertNotNull(wrapper.getExpression().getOrderBy());
        assertEquals(2, wrapper.getExpression().getOrderBy().size(), "默认排序应 sort asc + create_time desc");
        String full = fullSql(wrapper);
        assertTrue(full.contains("sort ASC") && full.contains("create_time DESC"), "默认排序列应正确: " + full);
    }

    @Test
    @DisplayName("管理端分页：sortField=createTime 时仅按创建时间倒序")
    void pageVideos_orderByCreateTime() {
        VideoPageQuery query = new VideoPageQuery();
        query.setSortField("createTime");
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.pageVideos(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "createTime 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("create_time DESC"), "应按创建时间倒序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("管理端分页：sortField=accessCount 时按浏览数倒序")
    void pageVideos_orderByAccessCount() {
        VideoPageQuery query = new VideoPageQuery();
        query.setSortField("accessCount");
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.pageVideos(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "accessCount 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("access_count DESC"), "应按浏览数倒序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("管理端分页：sortField=duration 时按时长倒序")
    void pageVideos_orderByDuration() {
        VideoPageQuery query = new VideoPageQuery();
        query.setSortField("duration");
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.pageVideos(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "duration 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("duration DESC"), "应按时长倒序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("管理端详情：含 videoUrl/简介")
    void getDetail_containsVideoUrl() {
        Video video = buildVideo(1L);
        doReturn(video).when(videoService).getById(1L);

        VideoDetailVO vo = videoService.getDetail(1L);

        assertNotNull(vo);
        assertEquals("养生操教学", vo.getTitle());
        assertEquals("https://cdn.example.com/video1.mp4", vo.getVideoUrl());
        assertEquals("跟着练，轻松养生", vo.getDescription());
    }

    @Test
    @DisplayName("管理端详情：已删除视为不存在")
    void getDetail_deletedThrows() {
        Video video = buildVideo(1L);
        video.setDeleted(1);
        doReturn(video).when(videoService).getById(1L);

        assertThrows(BizException.class, () -> videoService.getDetail(1L));
    }

    // App 端分页/详情

    @Test
    @DisplayName("App 分页：仅启用未删，keyword/isRecommended/isHot 过滤生效")
    void appPage_enabledOnlyAndFilters() {
        VideoAppQuery query = new VideoAppQuery();
        query.setKeyword("养生");
        query.setIsRecommended(1);
        query.setIsHot(0);
        Video video = buildVideo(1L);
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of(video));
        page.setTotal(1);
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        IPage<VideoAppItemVO> result = videoService.appPage(query);

        assertEquals(1, result.getTotal());
        assertEquals("养生操教学", result.getRecords().get(0).getTitle());

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        String sql = whereSql(wrapper);
        assertTrue(sql.contains("deleted = "), "应过滤未删除: " + sql);
        assertTrue(sql.contains("is_enable = "), "App 应仅取启用: " + sql);
        assertTrue(sql.contains("title"), "应按标题模糊: " + sql);
        assertTrue(sql.contains("is_recommended"), "应按推荐过滤: " + sql);
        assertTrue(sql.contains("is_hot"), "应按热门过滤: " + sql);
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1), "is_enable=1 应入参");
    }

    @Test
    @DisplayName("App 分页：sortField=createTime rule=1(升序) → SQL 含 create_time ASC")
    void appPage_orderByCreateTimeAsc() {
        VideoAppQuery query = new VideoAppQuery();
        query.setSortField("createTime");
        query.setOrderByRule(1);
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.appPage(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "createTime 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("create_time ASC"), "rule=1 应按创建时间升序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("App 分页：sortField=accessCount rule=1(升序) → SQL 含 access_count ASC")
    void appPage_orderByAccessCountAsc() {
        VideoAppQuery query = new VideoAppQuery();
        query.setSortField("accessCount");
        query.setOrderByRule(1);
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.appPage(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "accessCount 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("access_count ASC"), "rule=1 应按浏览数升序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("App 分页：综合(未传 sortField) rule=1 仍固定 sort asc+create_time desc,不受规则影响")
    void appPage_comprehensiveFixedIgnoringRule() {
        VideoAppQuery query = new VideoAppQuery();
        query.setOrderByRule(1);
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.appPage(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(2, wrapper.getExpression().getOrderBy().size(), "综合应 sort asc + create_time desc 两列");
        String full = fullSql(wrapper);
        assertTrue(full.contains("sort ASC") && full.contains("create_time DESC"),
                "综合应固定倒序不受 rule 影响: " + full);
    }

    @Test
    @DisplayName("App 详情：启用视频可读，访问量 SQL 原子自增，返回含 videoUrl")
    void appDetail_visibleIncrementsAccessCount() {
        Video video = buildVideo(1L);
        doReturn(video).when(videoService).getById(1L);

        VideoAppDetailVO vo = videoService.appDetail(1L);

        assertNotNull(vo);
        assertEquals("https://cdn.example.com/video1.mp4", vo.getVideoUrl());
        assertEquals("跟着练，轻松养生", vo.getDescription());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<UpdateWrapper<Video>> captor = ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(videoService).update(captor.capture());
        String setSql = captor.getValue().getSqlSet().replaceAll("\\s+", "");
        assertTrue(setSql.contains("access_count=access_count+1"),
                "访问量应 SQL 原子自增: " + setSql);
        // 先触发 WHERE 段合并，再断言 id 入参
        String where = captor.getValue().getExpression().getNormal().getSqlSegment();
        assertTrue(where.contains("id"), "应按 id 更新: " + where);
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(1L),
                "应按 id 更新, params=" + captor.getValue().getParamNameValuePairs());
    }

    @Test
    @DisplayName("App 详情：已删除视频视为不存在")
    void appDetail_deletedThrows() {
        Video video = buildVideo(1L);
        video.setDeleted(1);
        doReturn(video).when(videoService).getById(1L);

        assertThrows(BizException.class, () -> videoService.appDetail(1L));
        verify(videoService, never()).update(any(Wrapper.class));
    }

    @Test
    @DisplayName("App 详情：未启用视频视为已下架不可播")
    void appDetail_disabledThrows() {
        Video video = buildVideo(1L);
        video.setIsEnable(0);
        doReturn(video).when(videoService).getById(1L);

        assertThrows(BizException.class, () -> videoService.appDetail(1L));
        verify(videoService, never()).update(any(Wrapper.class));
    }

    // 新增 / 编辑 / 启用 / 删除

    @Test
    @DisplayName("管理端新增：默认启用并初始化计数/推荐/热度/排序/时长")
    void createVideo_defaultsApplied() {
        VideoSaveDTO dto = new VideoSaveDTO();
        dto.setTitle("八段锦教学");
        dto.setVideoUrl("https://cdn.example.com/v2.mp4");

        videoService.createVideo(dto);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoService).save(captor.capture());
        Video saved = captor.getValue();
        assertEquals(1, saved.getIsEnable(), "管理端新增默认启用");
        assertEquals(0, saved.getIsRecommended(), "默认不推荐");
        assertEquals(0, saved.getIsHot(), "默认不热门");
        assertEquals(0, saved.getSort());
        assertEquals(0, saved.getDuration(), "未填时长默认0");
        assertEquals(0, saved.getAccessCount());
        assertEquals(0, saved.getDeleted());
        assertEquals("八段锦教学", saved.getTitle());
        assertEquals("https://cdn.example.com/v2.mp4", saved.getVideoUrl());
    }

    @Test
    @DisplayName("管理端编辑：只更新入参字段")
    void updateVideo_partialUpdate() {
        doReturn(buildVideo(1L)).when(videoService).getById(1L);
        VideoSaveDTO dto = new VideoSaveDTO();
        dto.setId(1L);
        dto.setTitle("改后标题");

        videoService.updateVideo(dto);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoService).updateById(captor.capture());
        Video upd = captor.getValue();
        assertEquals(1L, upd.getId());
        assertEquals("改后标题", upd.getTitle());
        assertNull(upd.getVideoUrl(), "编辑未传 video_url 不应携带");
    }

    @Test
    @DisplayName("管理端编辑：缺 id 拒绝")
    void updateVideo_missingIdThrows() {
        VideoSaveDTO dto = new VideoSaveDTO();
        dto.setTitle("无id");

        assertThrows(BizException.class, () -> videoService.updateVideo(dto));
    }

    @Test
    @DisplayName("运营启用/停用：按入参置 is_enable")
    void enableVideo_setsEnable() {
        doReturn(buildVideo(1L)).when(videoService).getById(1L);
        VideoEnableDTO on = new VideoEnableDTO();
        on.setIsEnable(1);
        VideoEnableDTO off = new VideoEnableDTO();
        off.setIsEnable(0);

        videoService.enableVideo(1L, on);
        videoService.enableVideo(1L, off);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoService, Mockito.times(2)).updateById(captor.capture());
        assertEquals(1, captor.getAllValues().get(0).getIsEnable(), "启用置 is_enable=1");
        assertEquals(0, captor.getAllValues().get(1).getIsEnable(), "停用置 is_enable=0");
    }

    @Test
    @DisplayName("运营启用：不存在视频拒绝")
    void enableVideo_missingThrows() {
        VideoEnableDTO on = new VideoEnableDTO();
        on.setIsEnable(1);

        assertThrows(BizException.class, () -> videoService.enableVideo(99L, on));
    }

    @Test
    @DisplayName("批量删除：逻辑删置 deleted=1")
    void deleteVideos_logicalDelete() {
        boolean ok = videoService.deleteVideos(List.of(1L, 2L, 3L));

        assertTrue(ok);
        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).update(captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        // 先触发 WHERE 段合并(in 入参延迟生成)，再断言 id 集合入参
        String where = whereSql(wrapper);
        assertTrue(where.contains("id") && where.contains("IN"), "应按 id IN 删除: " + where);
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1L), "应含删除 id 1");
        assertTrue(wrapper.getParamNameValuePairs().containsValue(3L), "应含删除 id 3");
        String setSql = wrapper.getSqlSet().replaceAll("\\s+", "");
        assertTrue(setSql.contains("deleted"), "应逻辑删置 deleted: " + setSql);
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1), "deleted=1 应入参");
    }

    @Test
    @DisplayName("批量删除：空集合不触库")
    void deleteVideos_emptyIdsNoOp() {
        assertTrue(videoService.deleteVideos(List.of()));
        verify(videoService, never()).update(any(Wrapper.class));
    }

    // Feign

    @Test
    @DisplayName("Feign 分页：默认仅启用，支持 keyword/isRecommended/isHot/sortField")
    void pageFeign_enabledByDefault() {
        VideoFeignPageQuery query = new VideoFeignPageQuery();
        query.setKeyword("养生");
        query.setIsRecommended(1);
        query.setSortField("accessCount");
        Video video = buildVideo(1L);
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of(video));
        page.setTotal(1);
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        IPage<VideoItemDTO> result = videoService.pageFeign(query);

        assertEquals(1, result.getTotal());
        assertEquals("养生操教学", result.getRecords().get(0).getTitle());
        assertEquals("https://cdn.example.com/video1.mp4", result.getRecords().get(0).getVideoUrl(),
                "Feign 数据项应含 video_url(装修器渲染用)");
        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        String sql = whereSql(wrapper);
        assertTrue(sql.contains("is_enable = "), "Feign 默认应仅取启用: " + sql);
        assertTrue(sql.contains("is_recommended"), "应按推荐过滤: " + sql);
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1), "is_enable=1 应入参");
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "accessCount 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("access_count DESC"), "应按浏览数倒序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("Feign 分页：sortField=duration rule=1(升序) → SQL 含 duration ASC")
    void pageFeign_orderByDurationAsc() {
        VideoFeignPageQuery query = new VideoFeignPageQuery();
        query.setSortField("duration");
        query.setOrderByRule(1);
        Video video = buildVideo(1L);
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of(video));
        page.setTotal(1);
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        IPage<VideoItemDTO> result = videoService.pageFeign(query);

        assertEquals(1, result.getTotal());
        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "duration 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("duration ASC"), "rule=1 应按时长升序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("Feign 分页：rule 缺省时单字段排序保持倒序兼容")
    void pageFeign_orderByRuleDefaultDesc() {
        VideoFeignPageQuery query = new VideoFeignPageQuery();
        query.setSortField("createTime");
        Page<Video> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(videoService).page(any(Page.class), any(Wrapper.class));

        videoService.pageFeign(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "createTime 排序应只有一列");
        assertTrue(fullSql(wrapper).contains("create_time DESC"), "rule 缺省应保持倒序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("Feign 按 ids 回显：空 ids 直接返回空")
    void listByIdsFeign_emptyIds() {
        assertEquals(0, videoService.listByIdsFeign(null).size());
        assertEquals(0, videoService.listByIdsFeign(List.of()).size());
        verify(videoService, never()).list(any(Wrapper.class));
    }

    @Test
    @DisplayName("Feign 按 ids 回显：仅启用未删，字段映射")
    void listByIdsFeign_enabledOnly() {
        Video video = buildVideo(1L);
        doReturn(List.of(video)).when(videoService).list(any(Wrapper.class));

        List<VideoItemDTO> items = videoService.listByIdsFeign(List.of(1L));

        assertEquals(1, items.size());
        assertEquals("养生操教学", items.get(0).getTitle());
        assertEquals("https://cdn.example.com/video1.mp4", items.get(0).getVideoUrl());
        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(videoService).list(captor.capture());
        String sql = whereSql(captor.getValue());
        assertTrue(sql.contains("is_enable = "), "回显应仅取启用: " + sql);
        assertTrue(sql.contains("deleted = "), "回显应过滤未删除: " + sql);
    }
}
