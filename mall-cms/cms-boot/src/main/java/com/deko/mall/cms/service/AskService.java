package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
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

import java.util.List;

/**
 * 商品问答服务
 */
public interface AskService extends IService<Ask> {

    /**
     * 管理端分页列表（含禁用，keyword/spuId/isReply/isEnable 过滤，排序见 AskPageQuery.sortField）
     */
    IPage<AskVO> pageAsks(AskPageQuery query);

    /**
     * 管理端详情（编辑回显，含 content/answer）
     */
    AskDetailVO getDetail(Long id);

    /**
     * 管理端新增（运营代录问题，默认启用）
     */
    boolean createAsk(AskSaveDTO dto);

    /**
     * 管理端编辑（标题/内容/商品快照/排序/启用可改；不改 answer/is_reply）
     */
    boolean updateAsk(AskSaveDTO dto);

    /**
     * 管理端批量删除（逻辑删除）
     */
    boolean deleteAsks(List<Long> ids);

    /**
     * 运营回复（置 answer/is_reply=1/replyUser/replyTime，同时自动 is_enable=1）
     */
    boolean replyAsk(Long id, AskReplyDTO dto);

    /**
     * 运营确认/停用（is_enable 0→1 的确认操作；is_reply 由回复接口管理）
     */
    boolean enableAsk(Long id, AskEnableDTO dto);

    /**
     * App 端分页列表（仅启用未删，spuId 空=全部启用问答）
     */
    IPage<AskAppItemVO> appPage(AskAppQuery query);

    /**
     * App 端详情（访问量+1；is_enable=0 且非提问人本人视为不存在）
     *
     * @param memberId 当前登录会员ID(未登录为 null)
     */
    AskAppDetailVO appDetail(Long id, Long memberId);

    /**
     * App 用户提问（登录态；is_enable=0 待运营确认，is_reply=0）
     *
     * @return 新建问答ID
     */
    Long askByUser(AskAppAskDTO dto, Long memberId);

    /**
     * Feign：分页问答（装修器选问答，默认仅启用）
     */
    IPage<AskItemDTO> pageFeign(AskFeignPageQuery query);

    /**
     * Feign：按 ids 回显（仅启用未删）
     */
    List<AskItemDTO> listByIdsFeign(List<Long> ids);
}
