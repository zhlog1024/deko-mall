package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
import com.deko.mall.cms.mapper.AskMapper;
import com.deko.mall.cms.model.dto.AskAppAskDTO;
import com.deko.mall.cms.model.dto.AskEnableDTO;
import com.deko.mall.cms.model.dto.AskReplyDTO;
import com.deko.mall.cms.model.dto.AskSaveDTO;
import com.deko.mall.cms.model.entity.Ask;
import com.deko.mall.cms.model.query.AskAppQuery;
import com.deko.mall.cms.model.query.AskPageQuery;
import com.deko.mall.cms.model.vo.AskAppDetailVO;
import com.deko.mall.cms.model.vo.AskAppItemVO;
import com.deko.mall.cms.model.vo.AskDetailVO;
import com.deko.mall.cms.model.vo.AskVO;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 商品问答服务实现
 */
@Slf4j
@Service
public class AskServiceImpl extends ServiceImpl<AskMapper, Ask> implements AskService {

    @Override
    public IPage<AskVO> pageAsks(AskPageQuery query) {
        Page<Ask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Ask> wrapper = buildBaseWrapper();
        applyCommonFilters(wrapper, query.getKeyword(), query.getSpuId(), query.getIsReply());
        if (query.getIsEnable() != null) {
            wrapper.eq(Ask::getIsEnable, query.getIsEnable());
        }
        applyOrder(wrapper, query.getSortField());

        IPage<Ask> result = page(page, wrapper);
        List<AskVO> voList = result.getRecords().stream().map(this::toAdminVO).toList();

        Page<AskVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AskDetailVO getDetail(Long id) {
        Ask ask = requireAsk(id);
        AskDetailVO vo = new AskDetailVO();
        BeanUtils.copyProperties(ask, vo);
        return vo;
    }

    @Override
    public boolean createAsk(AskSaveDTO dto) {
        Ask ask = new Ask();
        BeanUtils.copyProperties(dto, ask);
        if (ask.getIsEnable() == null) {
            ask.setIsEnable(1);
        }
        if (ask.getSort() == null) {
            ask.setSort(0);
        }
        ask.setIsReply(0);
        ask.setAccessCount(0);
        ask.setDeleted(0);
        return save(ask);
    }

    @Override
    public boolean updateAsk(AskSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("问答ID不能为空");
        }
        requireAsk(dto.getId());
        Ask ask = new Ask();
        BeanUtils.copyProperties(dto, ask);
        // 局部更新：dto 未携带的字段(含 answer/is_reply)不触碰
        return updateById(ask);
    }

    @Override
    public boolean deleteAsks(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        LambdaUpdateWrapper<Ask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Ask::getId, ids).set(Ask::getDeleted, 1);
        return update(wrapper);
    }

    @Override
    public boolean replyAsk(Long id, AskReplyDTO dto) {
        requireAsk(id);
        Ask ask = new Ask();
        ask.setId(id);
        ask.setAnswer(dto.getAnswer());
        ask.setIsReply(1);
        ask.setReplyTime(LocalDateTime.now());
        // 回复即确认展示
        ask.setIsEnable(1);
        // 回答人快照：入参优先，空则取当前登录人（运营后台）
        String replyUser = StringUtils.isBlank(dto.getReplyUser()) ? SecurityUtils.getUsername() : dto.getReplyUser();
        ask.setReplyUser(replyUser);
        return updateById(ask);
    }

    @Override
    public boolean enableAsk(Long id, AskEnableDTO dto) {
        requireAsk(id);
        Ask ask = new Ask();
        ask.setId(id);
        ask.setIsEnable(dto.getIsEnable());
        return updateById(ask);
    }

    @Override
    public IPage<AskAppItemVO> appPage(AskAppQuery query) {
        Page<Ask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Ask> wrapper = buildBaseWrapper();
        wrapper.eq(Ask::getIsEnable, 1);
        applyCommonFilters(wrapper, query.getKeyword(), query.getSpuId(), query.getIsReply());
        applyDiyOrder(wrapper, query.getOrderByType(), query.getOrderByRule());

        IPage<Ask> result = page(page, wrapper);
        List<AskAppItemVO> voList = result.getRecords().stream().map(this::toAppItemVO).toList();

        Page<AskAppItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AskAppDetailVO appDetail(Long id, Long memberId) {
        Ask ask = requireAsk(id);
        // 未启用(运营待确认/停用)：仅提问人本人可见，其余视为不存在
        boolean enabled = ask.getIsEnable() != null && ask.getIsEnable() == 1;
        if (!enabled) {
            boolean owner = memberId != null && ask.getAskUserId() != null && Objects.equals(memberId, ask.getAskUserId());
            if (!owner) {
                throw new BizException("问答不存在");
            }
        }
        // 访问量自增（SQL 原子更新，不触发字段自动填充）
        UpdateWrapper<Ask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).setSql("access_count = access_count + 1");
        update(updateWrapper);

        AskAppDetailVO vo = new AskAppDetailVO();
        BeanUtils.copyProperties(ask, vo);
        return vo;
    }

    @Override
    public Long askByUser(AskAppAskDTO dto, Long memberId) {
        if (memberId == null) {
            throw new BizException("请先登录后再提问");
        }
        Ask ask = new Ask();
        ask.setSpuId(dto.getSpuId());
        ask.setSpuName(dto.getSpuName());
        ask.setSpuPic(dto.getSpuPic());
        ask.setTitle(dto.getTitle());
        ask.setContent(dto.getContent());
        ask.setAskUserId(memberId);
        // token 内无会员昵称，ask_user 快照留空由前端兜底"匿名"
        ask.setIsEnable(0);
        ask.setIsReply(0);
        ask.setAccessCount(0);
        ask.setSort(0);
        ask.setDeleted(0);
        save(ask);
        return ask.getId();
    }

    @Override
    public IPage<AskItemDTO> pageFeign(AskFeignPageQuery query) {
        Page<Ask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Ask> wrapper = buildBaseWrapper();
        Integer isEnable = query.getIsEnable() == null ? 1 : query.getIsEnable();
        wrapper.eq(Ask::getIsEnable, isEnable);
        applyCommonFilters(wrapper, query.getKeyword(), query.getSpuId(), query.getIsReply());
        applyDiyOrder(wrapper, query.getOrderByType(), query.getOrderByRule());

        IPage<Ask> result = page(page, wrapper);
        List<AskItemDTO> dtoList = result.getRecords().stream().map(this::toFeignItem).toList();

        Page<AskItemDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public List<AskItemDTO> listByIdsFeign(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Ask> wrapper = buildBaseWrapper();
        wrapper.in(Ask::getId, ids).eq(Ask::getIsEnable, 1);
        return list(wrapper).stream().map(this::toFeignItem).toList();
    }

    // 私有工具

    private LambdaQueryWrapper<Ask> buildBaseWrapper() {
        LambdaQueryWrapper<Ask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ask::getDeleted, 0);
        return wrapper;
    }

    /**
     * keyword(标题模糊)/spuId/isReply 公共过滤（admin/app/feign 共用）
     */
    private void applyCommonFilters(LambdaQueryWrapper<Ask> wrapper, String keyword, Long spuId, Integer isReply) {
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Ask::getTitle, keyword);
        }
        if (spuId != null) {
            wrapper.eq(Ask::getSpuId, spuId);
        }
        if (isReply != null) {
            wrapper.eq(Ask::getIsReply, isReply);
        }
    }

    /**
     * 排序：createTime=创建时间倒序；默认自定义排序升序(sort)优先 + 创建时间倒序（管理端 AskPageQuery.sortField 用）
     */
    private void applyOrder(LambdaQueryWrapper<Ask> wrapper, String sortField) {
        if ("createTime".equals(sortField)) {
            wrapper.orderByDesc(Ask::getCreateTime);
            return;
        }
        wrapper.orderByAsc(Ask::getSort).orderByDesc(Ask::getCreateTime);
    }

    /**
     * DIY 排序契约（orderByType/orderByRule 数值与编辑器选项一致，app/feign 通道共用）：
     * orderByType 0=综合(默认) 固定 sort asc + create_time desc，不受 orderByRule 影响；
     * orderByType 1=最新 按 create_time，方向由 orderByRule 决定（0=降序/1=升序，缺省0降序）
     */
    private void applyDiyOrder(LambdaQueryWrapper<Ask> wrapper, Integer orderByType, Integer orderByRule) {
        boolean asc = orderByRule != null && orderByRule == 1;
        if (orderByType != null && orderByType == 1) {
            wrapper.orderBy(true, asc, Ask::getCreateTime);
            return;
        }
        wrapper.orderByAsc(Ask::getSort).orderByDesc(Ask::getCreateTime);
    }

    private Ask requireAsk(Long id) {
        Ask ask = getById(id);
        if (ask == null || ask.getDeleted() != null && ask.getDeleted() == 1) {
            throw new BizException("问答不存在");
        }
        return ask;
    }

    private AskVO toAdminVO(Ask ask) {
        AskVO vo = new AskVO();
        BeanUtils.copyProperties(ask, vo);
        return vo;
    }

    private AskAppItemVO toAppItemVO(Ask ask) {
        AskAppItemVO vo = new AskAppItemVO();
        BeanUtils.copyProperties(ask, vo);
        return vo;
    }

    private AskItemDTO toFeignItem(Ask ask) {
        AskItemDTO dto = new AskItemDTO();
        BeanUtils.copyProperties(ask, dto);
        return dto;
    }
}
