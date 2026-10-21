package com.deko.mall.pms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.config.PmsResultCode;
import com.deko.mall.pms.mapper.PmsGoodsCommentMapper;
import com.deko.mall.ums.api.MemberFeignClient;
import com.deko.mall.ums.dto.MemberInfoDTO;
import com.deko.mall.pms.model.entity.PmsGoodsComment;
import com.deko.mall.pms.model.form.CommentAuditForm;
import com.deko.mall.pms.model.form.CommentReplyForm;
import com.deko.mall.pms.model.form.CommentSubmitForm;
import com.deko.mall.pms.model.query.CommentPageQuery;
import com.deko.mall.pms.model.vo.CommentScoreVO;
import com.deko.mall.pms.model.vo.GoodsCommentVO;
import com.deko.mall.pms.service.GoodsCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品评论 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsCommentServiceImpl extends ServiceImpl<PmsGoodsCommentMapper, PmsGoodsComment>
        implements GoodsCommentService {

    private final PmsGoodsCommentMapper commentMapper;

    private final MemberFeignClient memberFeignClient;

    @Override
    public IPage<GoodsCommentVO> pageComments(CommentPageQuery query) {
        Page<PmsGoodsComment> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<GoodsCommentVO> voPage = commentMapper.pageComments(page, query);
        fillCommentImages(voPage.getRecords());
        return voPage;
    }

    @Override
    public GoodsCommentVO getCommentDetail(Long id) {
        PmsGoodsComment entity = this.getById(id);
        if (entity == null) {
            throw new BizException(PmsResultCode.COMMENT_NOT_FOUND);
        }
        GoodsCommentVO vo = new GoodsCommentVO();
        vo.setId(entity.getId());
        vo.setSpuId(entity.getSpuId());
        vo.setSkuId(entity.getSkuId());
        vo.setOrderId(entity.getOrderId());
        vo.setOrderNo(entity.getOrderNo());
        vo.setMemberId(entity.getMemberId());
        vo.setRating(entity.getRating());
        vo.setContent(entity.getContent());
        if (StrUtil.isNotBlank(entity.getImages())) {
            vo.setImages(cn.hutool.json.JSONUtil.toList(entity.getImages(), String.class));
        }
        vo.setIsAnonymous(entity.getIsAnonymous());
        vo.setIsReplied(entity.getIsReplied());
        vo.setReplyContent(entity.getReplyContent());
        vo.setReplyTime(entity.getReplyTime());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitComments(Long orderId, CommentSubmitForm form) {
        Long memberId = SecurityUtils.getMemberId();
        if (memberId == null) {
            throw new BizException(PmsResultCode.LOGIN_REQUIRED);
        }

        List<CommentSubmitForm.CommentItem> items = form.getItems();
        if (CollectionUtil.isEmpty(items)) {
            throw new BizException(PmsResultCode.COMMENT_ITEMS_EMPTY);
        }

        for (CommentSubmitForm.CommentItem item : items) {
            // 检查是否已评论
            Long count = this.count(new LambdaQueryWrapper<PmsGoodsComment>()
                    .eq(PmsGoodsComment::getOrderId, orderId)
                    .eq(PmsGoodsComment::getSkuId, item.getSkuId())
                    .eq(PmsGoodsComment::getMemberId, memberId)
                    .eq(PmsGoodsComment::getDeleted, 0));
            if (count > 0) {
                throw new BizException(PmsResultCode.COMMENT_ALREADY_EXISTS);
            }

            PmsGoodsComment comment = new PmsGoodsComment();
            comment.setSpuId(item.getSpuId());
            comment.setSkuId(item.getSkuId());
            comment.setOrderId(orderId);
            comment.setMemberId(memberId);
            // 保存会员昵称和头像（冗余存储，避免跨服务查询）
            try {
                Result<MemberInfoDTO> memberInfo = memberFeignClient.info(memberId);
                if (memberInfo != null && memberInfo.getData() != null) {
                    comment.setMemberName(memberInfo.getData().getNickName());
                    comment.setMemberAvatar(memberInfo.getData().getAvatarUrl());
                }
            } catch (Exception e) {
                log.warn("获取会员信息失败, memberId: {}", memberId, e);
            }
            comment.setRating(item.getRating());
            comment.setContent(item.getContent());
            if (CollectionUtil.isNotEmpty(item.getImages())) {
                comment.setImages(cn.hutool.json.JSONUtil.toJsonStr(item.getImages()));
            }
            comment.setIsAnonymous(form.getIsAnonymous() != null ? form.getIsAnonymous() : 0);
            comment.setIsReplied(0);
            comment.setStatus(1); // 默认审核通过
            comment.setDeleted(0);

            this.save(comment);

            // 更新订单商品评论状态
            updateOrderItemCommented(item.getOrderItemId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(Long id, CommentReplyForm form) {
        PmsGoodsComment comment = this.getById(id);
        if (comment == null) {
            throw new BizException(PmsResultCode.COMMENT_NOT_FOUND);
        }
        LambdaUpdateWrapper<PmsGoodsComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PmsGoodsComment::getId, id)
                .set(PmsGoodsComment::getIsReplied, 1)
                .set(PmsGoodsComment::getReplyContent, form.getReplyContent())
                .set(PmsGoodsComment::getReplyTime, LocalDateTime.now());
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, CommentAuditForm form) {
        PmsGoodsComment comment = this.getById(id);
        if (comment == null) {
            throw new BizException(PmsResultCode.COMMENT_NOT_FOUND);
        }
        LambdaUpdateWrapper<PmsGoodsComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PmsGoodsComment::getId, id)
                .set(PmsGoodsComment::getStatus, form.getStatus());
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        LambdaUpdateWrapper<PmsGoodsComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PmsGoodsComment::getId, id)
                .set(PmsGoodsComment::getDeleted, 1);
        this.update(updateWrapper);
    }

    @Override
    public IPage<GoodsCommentVO> pageAppComments(Long spuId, Integer rating, Boolean hasImage,
                                                  Integer pageNum, Integer pageSize) {
        Page<PmsGoodsComment> page = new Page<>(pageNum, pageSize);
        IPage<GoodsCommentVO> voPage = commentMapper.pageAppComments(page, spuId, rating, hasImage);
        // 匿名评论隐藏用户信息 + 填充图片
        fillCommentImages(voPage.getRecords());
        for (GoodsCommentVO vo : voPage.getRecords()) {
            if (vo.getIsAnonymous() != null && vo.getIsAnonymous() == 1) {
                vo.setMemberName("匿名用户");
                vo.setMemberAvatar(null);
            }
        }
        return voPage;
    }

    @Override
    public CommentScoreVO getCommentScore(Long spuId) {
        CommentScoreVO scoreVO = commentMapper.getCommentScore(spuId);
        if (scoreVO == null) {
            scoreVO = new CommentScoreVO();
            scoreVO.setAvg(java.math.BigDecimal.ZERO);
            scoreVO.setTotal(0L);
            scoreVO.setRating(List.of());
            return scoreVO;
        }
        // 计算各星级评分分布
        List<CommentScoreVO.RatingItem> ratingItems = new java.util.ArrayList<>();
        String[] names = {"5星", "4星", "3星", "2星", "1星"};
        for (int i = 5; i >= 1; i--) {
            final int star = i;
            Long count = this.count(new LambdaQueryWrapper<PmsGoodsComment>()
                    .eq(PmsGoodsComment::getSpuId, spuId)
                    .eq(PmsGoodsComment::getRating, star)
                    .eq(PmsGoodsComment::getStatus, 1)
                    .eq(PmsGoodsComment::getDeleted, 0));
            CommentScoreVO.RatingItem item = new CommentScoreVO.RatingItem();
            item.setName(names[5 - star]);
            item.setCount(count);
            if (scoreVO.getTotal() > 0) {
                item.setPortion(java.math.BigDecimal.valueOf(count)
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .divide(java.math.BigDecimal.valueOf(scoreVO.getTotal()), 1, java.math.RoundingMode.HALF_UP));
            } else {
                item.setPortion(java.math.BigDecimal.ZERO);
            }
            ratingItems.add(item);
        }
        scoreVO.setRating(ratingItems);
        return scoreVO;
    }

    @Override
    public IPage<GoodsCommentVO> pageMemberComments(Integer pageNum, Integer pageSize) {
        Long memberId = SecurityUtils.getMemberId();
        if (memberId == null) {
            throw new BizException(PmsResultCode.LOGIN_REQUIRED);
        }
        Page<PmsGoodsComment> page = new Page<>(pageNum, pageSize);
        IPage<GoodsCommentVO> voPage = commentMapper.pageMemberComments(page, memberId);
        fillCommentImages(voPage.getRecords());
        return voPage;
    }

    /**
     * 批量填充评论图片（JSON字符串 → List）和匿名处理
     */
    private void fillCommentImages(List<GoodsCommentVO> vos) {
        if (CollectionUtil.isEmpty(vos)) return;
        List<Long> ids = vos.stream().map(GoodsCommentVO::getId).toList();
        List<PmsGoodsComment> entities = this.listByIds(ids);
        var entityMap = entities.stream().collect(Collectors.toMap(PmsGoodsComment::getId, e -> e));
        for (GoodsCommentVO vo : vos) {
            PmsGoodsComment entity = entityMap.get(vo.getId());
            if (entity != null && StrUtil.isNotBlank(entity.getImages())) {
                vo.setImages(cn.hutool.json.JSONUtil.toList(entity.getImages(), String.class));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMyComment(Long id) {
        Long memberId = SecurityUtils.getMemberId();
        if (memberId == null) {
            throw new BizException(PmsResultCode.LOGIN_REQUIRED);
        }
        PmsGoodsComment comment = this.getById(id);
        if (comment == null || !comment.getMemberId().equals(memberId)) {
            throw new BizException(PmsResultCode.COMMENT_NOT_FOUND);
        }
        LambdaUpdateWrapper<PmsGoodsComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PmsGoodsComment::getId, id)
                .set(PmsGoodsComment::getDeleted, 1);
        this.update(updateWrapper);
    }

    /**
     * 更新订单商品项评论状态
     */
    private void updateOrderItemCommented(Long orderItemId) {
        commentMapper.updateOrderItemCommented(orderItemId);
    }
}
