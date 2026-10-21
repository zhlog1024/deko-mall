package com.deko.mall.cms.service.impl;

import com.deko.mall.cms.mapper.VideoCommentMapper;
import com.deko.mall.cms.mapper.VideoLikeMapper;
import com.deko.mall.cms.mapper.VideoMapper;
import com.deko.mall.cms.mapper.VideoReportMapper;
import com.deko.mall.cms.model.entity.Video;
import com.deko.mall.cms.model.entity.VideoComment;
import com.deko.mall.cms.model.entity.VideoLike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 视频互动服务单测（详情/点赞切换/评论/举报）
 */
class VideoInteractServiceImplTest {

    private VideoInteractServiceImpl service;

    @Mock
    private VideoMapper videoMapper;
    @Mock
    private VideoLikeMapper videoLikeMapper;
    @Mock
    private VideoCommentMapper videoCommentMapper;
    @Mock
    private VideoReportMapper videoReportMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VideoInteractServiceImpl(videoMapper, videoLikeMapper, videoCommentMapper, videoReportMapper);
    }

    private Video video(Long id) {
        Video v = new Video();
        v.setId(id);
        v.setTitle("测试视频" + id);
        v.setCover("http://img/" + id + ".jpg");
        v.setVideoUrl("http://video/" + id + ".mp4");
        v.setIsEnable(1);
        v.setDeleted(0);
        v.setSpuId(100L);
        v.setSpuName("关联商品");
        v.setSpuPrice(new BigDecimal("9.90"));
        return v;
    }

    private void stubCount(Long likeCount, Long commentCount) {
        when(videoLikeMapper.selectCount(any())).thenReturn(likeCount);
        when(videoCommentMapper.selectCount(any())).thenReturn(commentCount);
    }

    @Test
    @DisplayName("pluginDetail：条目含互动计数/商品卡/配置开关")
    void pluginDetail_containsItemAndConfig() {
        when(videoMapper.selectById(1L)).thenReturn(video(1L));
        stubCount(3L, 5L);
        Map<String, Object> result = service.pluginDetail(1L, null);
        assertNotNull(result.get("data"));
        assertNotNull(result.get("base_config_data"));
        assertNotNull(result.get("report_type_list"));
        Map<String, Object> cfg = (Map<String, Object>) result.get("base_config_data");
        assertEquals(1, cfg.get("is_video_give_thumbs"));
        // data 序列化为 Map 后关键字段存在（jackson 会把 VO 转 Map？此处返回 VO 对象,直接断言类字段经由 Map 序列化交由 controller；service 层校验结构键即可）
        com.deko.mall.cms.model.vo.VideoPluginItemVO item =
                (com.deko.mall.cms.model.vo.VideoPluginItemVO) result.get("data");
        assertEquals(1L, item.getId());
        assertEquals("关联商品", item.getGoods().getTitle());
        assertEquals(3, item.getGiveThumbsCount());
        assertEquals(5, item.getCommentsCount());
        assertFalse(item.getIsGiveThumbs()); // 游客未点赞
    }

    @Test
    @DisplayName("lastNext：返回 last/next 两个列表结构")
    void lastNext_returnsBothArrays() {
        when(videoMapper.selectById(2L)).thenReturn(video(2L));
        // 无上一条/下一条
        when(videoMapper.selectList(any())).thenReturn(java.util.List.of());
        Map<String, Object> result = service.lastNext(2L, true, true, null);
        assertTrue(result.containsKey("last"));
        assertTrue(result.containsKey("next"));
        assertEquals(0, ((java.util.List<?>) result.get("last")).size());
    }

    @Test
    @DisplayName("giveThumbs：首次赞 is_active=true，再点取消")
    void giveThumbs_toggle() {
        when(videoMapper.selectById(1L)).thenReturn(video(1L));
        // 第一次：无记录 → insert → active
        when(videoLikeMapper.selectOne(any())).thenReturn(null);
        when(videoLikeMapper.selectCount(any())).thenReturn(1L);
        Map<String, Object> r1 = service.giveThumbs(1L, null, 9L);
        assertTrue((Boolean) r1.get("is_active"));
        // 第二次：已有记录 → delete → inactive
        VideoLike existing = new VideoLike();
        existing.setId(1L);
        when(videoLikeMapper.selectOne(any())).thenReturn(existing);
        when(videoLikeMapper.selectCount(any())).thenReturn(0L);
        Map<String, Object> r2 = service.giveThumbs(1L, null, 9L);
        assertFalse((Boolean) r2.get("is_active"));
    }

    @Test
    @DisplayName("addComment：空内容拒绝")
    void addComment_emptyRejected() {
        when(videoMapper.selectById(1L)).thenReturn(video(1L));
        assertThrows(IllegalArgumentException.class, () -> service.addComment(1L, 0L, "  ", null, 9L));
        verify(videoCommentMapper, never()).insert(any());
    }

    @Test
    @DisplayName("deleteComment：非本人拒绝")
    void deleteComment_notOwnerRejected() {
        VideoComment c = new VideoComment();
        c.setId(1L);
        c.setUserId(5L);
        c.setDeleted(0);
        when(videoCommentMapper.selectById(1L)).thenReturn(c);
        assertThrows(IllegalArgumentException.class, () -> service.deleteComment(1L, 9L));
    }

    @Test
    @DisplayName("report：入库")
    void report_insert() {
        when(videoMapper.selectById(1L)).thenReturn(video(1L));
        service.report(1L, "侵权", "盗用内容", 9L);
        verify(videoReportMapper).insert(any());
    }
}
