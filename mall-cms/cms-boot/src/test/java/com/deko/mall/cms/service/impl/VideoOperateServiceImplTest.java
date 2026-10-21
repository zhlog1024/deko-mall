package com.deko.mall.cms.service.impl;

import com.deko.mall.cms.mapper.VideoCommentMapper;
import com.deko.mall.cms.mapper.VideoMapper;
import com.deko.mall.cms.mapper.VideoReportMapper;
import com.deko.mall.cms.model.entity.VideoComment;
import com.deko.mall.cms.model.entity.VideoReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 视频运营管理服务单测（评论删除级联/启停/举报处理）
 */
class VideoOperateServiceImplTest {

    private VideoOperateServiceImpl service;

    @Mock
    private VideoCommentMapper commentMapper;
    @Mock
    private VideoReportMapper reportMapper;
    @Mock
    private VideoMapper videoMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VideoOperateServiceImpl(commentMapper, reportMapper, videoMapper);
    }

    @Test
    @DisplayName("删除评论：不存在报错")
    void deleteComment_missingThrows() {
        when(commentMapper.selectById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.deleteComment(1L));
        verify(commentMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("删除评论：存在则逻辑删")
    void deleteComment_ok() {
        VideoComment c = new VideoComment();
        c.setId(1L);
        c.setDeleted(0);
        when(commentMapper.selectById(1L)).thenReturn(c);
        // 无子评论
        when(commentMapper.selectList(any())).thenReturn(java.util.List.of());
        service.deleteComment(1L);
        verify(commentMapper).updateById(any());
    }

    @Test
    @DisplayName("举报处理：不存在报错，存在更新状态")
    void handleReport() {
        when(reportMapper.selectById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.handleReport(1L, 1));

        VideoReport r = new VideoReport();
        r.setId(1L);
        r.setDeleted(0);
        when(reportMapper.selectById(2L)).thenReturn(r);
        service.handleReport(2L, 1);
        verify(reportMapper).updateById(any());
    }
}
