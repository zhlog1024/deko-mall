package com.deko.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.entity.PmsGoodsComment;
import com.deko.mall.pms.model.query.CommentPageQuery;
import com.deko.mall.pms.model.vo.CommentScoreVO;
import com.deko.mall.pms.model.vo.GoodsCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品评论 Mapper
 */
@Mapper
public interface PmsGoodsCommentMapper extends BaseMapper<PmsGoodsComment> {

    IPage<GoodsCommentVO> pageComments(Page<PmsGoodsComment> page, @Param("query") CommentPageQuery query);

    CommentScoreVO getCommentScore(@Param("spuId") Long spuId);

    void updateOrderItemCommented(@Param("orderItemId") Long orderItemId);

    IPage<GoodsCommentVO> pageMemberComments(Page<PmsGoodsComment> page, @Param("memberId") Long memberId);

    IPage<GoodsCommentVO> pageAppComments(Page<PmsGoodsComment> page, @Param("spuId") Long spuId,
                                          @Param("rating") Integer rating, @Param("hasImage") Boolean hasImage);
}
