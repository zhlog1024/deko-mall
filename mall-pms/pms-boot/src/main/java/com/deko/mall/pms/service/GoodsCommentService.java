package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.entity.PmsGoodsComment;
import com.deko.mall.pms.model.form.CommentAuditForm;
import com.deko.mall.pms.model.form.CommentReplyForm;
import com.deko.mall.pms.model.form.CommentSubmitForm;
import com.deko.mall.pms.model.query.CommentPageQuery;
import com.deko.mall.pms.model.vo.CommentScoreVO;
import com.deko.mall.pms.model.vo.GoodsCommentVO;

/**
 * 商品评论 Service
 */
public interface GoodsCommentService extends IService<PmsGoodsComment> {

    IPage<GoodsCommentVO> pageComments(CommentPageQuery query);

    GoodsCommentVO getCommentDetail(Long id);

    void submitComments(Long orderId, CommentSubmitForm form);

    void reply(Long id, CommentReplyForm form);

    void audit(Long id, CommentAuditForm form);

    void deleteComment(Long id);

    IPage<GoodsCommentVO> pageAppComments(Long spuId, Integer rating, Boolean hasImage, Integer pageNum, Integer pageSize);

    CommentScoreVO getCommentScore(Long spuId);

    IPage<GoodsCommentVO> pageMemberComments(Integer pageNum, Integer pageSize);

    void deleteMyComment(Long id);
}
