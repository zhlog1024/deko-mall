package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 商品问答服务单元测试（mock mapper，覆盖关键分支）
 * <p>
 * 覆盖：admin/app/feign 分页过滤分支、详情访问量自增、回复置位、启用确认、
 * 用户提问 is_enable=0、逻辑删除。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AskServiceImplTest {

    private AskServiceImpl askService;

    @Mock
    private AskMapper askMapper;

    @BeforeEach
    void setUp() {
        // 纯单元测试初始化 MyBatis-Plus lambda 缓存（否则 Ask::getXxx 方法引用报错）
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(
                        new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""),
                Ask.class);
        askService = Mockito.spy(new AskServiceImpl());
        // 桩：写库方法（单测边界，真实 DB 交互不在此验证）
        doReturn(true).when(askService).save(any());
        doReturn(true).when(askService).updateById(any());
        doReturn(true).when(askService).update(any(Wrapper.class));
        // 桩：getById 兜底（各测试按需覆盖）
        doReturn(null).when(askService).getById(any());
    }

    private Ask buildAsk(Long id) {
        Ask ask = new Ask();
        ask.setId(id);
        ask.setSpuId(10L);
        ask.setSpuName("灵芝孢子粉");
        ask.setSpuPic("https://cdn.example.com/spu10.png");
        ask.setTitle("如何食用？");
        ask.setContent("每日用量多少合适");
        ask.setAnswer("每次2克，温水冲服");
        ask.setIsReply(1);
        ask.setAccessCount(6);
        ask.setAskUserId(88L);
        ask.setReplyUser("运营小二");
        ask.setReplyTime(LocalDateTime.of(2026, 9, 1, 10, 0));
        ask.setSort(0);
        ask.setIsEnable(1);
        ask.setDeleted(0);
        ask.setCreateTime(LocalDateTime.of(2026, 8, 20, 9, 0));
        return ask;
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
    @DisplayName("管理端分页：keyword/spuId/isReply/isEnable 过滤组合生效")
    void pageAsks_allFiltersApplied() {
        AskPageQuery query = new AskPageQuery();
        query.setKeyword("食用");
        query.setSpuId(10L);
        query.setIsReply(1);
        query.setIsEnable(1);

        Ask ask = buildAsk(1L);
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of(ask));
        page.setTotal(1);
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        IPage<AskVO> result = askService.pageAsks(query);

        assertEquals(1, result.getTotal());
        assertEquals("如何食用？", result.getRecords().get(0).getTitle());

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        String sql = whereSql(wrapper);
        assertTrue(sql.contains("deleted = "), "应过滤未删除: " + sql);
        assertTrue(sql.contains("title"), "应按标题模糊: " + sql);
        assertTrue(sql.contains("spu_id"), "应按 spu 过滤: " + sql);
        assertTrue(sql.contains("is_reply"), "应按是否回复过滤: " + sql);
        assertTrue(sql.contains("is_enable"), "应按启用状态过滤: " + sql);
        assertTrue(wrapper.getParamNameValuePairs().containsValue(10L), "spuId 值应入参");
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1), "isReply/isEnable 值应入参");
    }

    @Test
    @DisplayName("管理端分页：无过滤条件仅未删除，默认自定义排序优先")
    void pageAsks_noFilterOnlyDeletedAndDefaultOrder() {
        AskPageQuery query = new AskPageQuery();
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        askService.pageAsks(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        String sql = whereSql(wrapper);
        assertTrue(sql.contains("deleted = "), "应过滤未删除: " + sql);
        assertFalse(sql.contains("title"), "无关键字不应出现 like: " + sql);
        // 默认排序：sort asc + create_time desc 两列
        assertNotNull(wrapper.getExpression().getOrderBy());
        assertEquals(2, wrapper.getExpression().getOrderBy().size(), "默认排序应 sort asc + create_time desc");
    }

    @Test
    @DisplayName("管理端分页：sortField=createTime 时仅按创建时间倒序")
    void pageAsks_orderByCreateTime() {
        AskPageQuery query = new AskPageQuery();
        query.setSortField("createTime");
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        askService.pageAsks(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        assertEquals(1, captor.getValue().getExpression().getOrderBy().size(),
                "createTime 排序应只有一列");
    }

    @Test
    @DisplayName("管理端详情：含 content/answer")
    void getDetail_containsContentAndAnswer() {
        Ask ask = buildAsk(1L);
        doReturn(ask).when(askService).getById(1L);

        AskDetailVO vo = askService.getDetail(1L);

        assertNotNull(vo);
        assertEquals("灵芝孢子粉", vo.getSpuName());
        assertEquals("每日用量多少合适", vo.getContent());
        assertEquals("每次2克，温水冲服", vo.getAnswer());
    }

    @Test
    @DisplayName("管理端详情：已删除视为不存在")
    void getDetail_deletedThrows() {
        Ask ask = buildAsk(1L);
        ask.setDeleted(1);
        doReturn(ask).when(askService).getById(1L);

        assertThrows(BizException.class, () -> askService.getDetail(1L));
    }

    // App 端分页/详情

    @Test
    @DisplayName("App 分页：仅启用未删，spuId 过滤")
    void appPage_enabledOnlyAndSpuFilter() {
        AskAppQuery query = new AskAppQuery();
        query.setSpuId(10L);
        query.setKeyword("食用");
        Ask ask = buildAsk(1L);
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of(ask));
        page.setTotal(1);
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        IPage<AskAppItemVO> result = askService.appPage(query);

        assertEquals(1, result.getTotal());
        assertEquals("如何食用？", result.getRecords().get(0).getTitle());

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        String sql = whereSql(wrapper);
        assertTrue(sql.contains("deleted = "), "应过滤未删除: " + sql);
        assertTrue(sql.contains("is_enable = "), "App 应仅取启用: " + sql);
        assertTrue(sql.contains("spu_id"), "应按 spu 过滤: " + sql);
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1), "is_enable=1 应入参");
    }

    @Test
    @DisplayName("App 分页：DIY orderByType=1(最新) rule=0 → 仅按创建时间倒序")
    void appPage_diyNewestDesc() {
        AskAppQuery query = new AskAppQuery();
        query.setOrderByType(1);
        query.setOrderByRule(0);
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        askService.appPage(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "最新应只有 create_time 一列");
        assertTrue(fullSql(wrapper).contains("create_time DESC"), "最新 rule=0 应倒序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("App 分页：DIY orderByType=1(最新) rule=1(升序) → SQL 含 create_time ASC")
    void appPage_diyNewestAsc() {
        AskAppQuery query = new AskAppQuery();
        query.setOrderByType(1);
        query.setOrderByRule(1);
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        askService.appPage(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "最新应只有 create_time 一列");
        assertTrue(fullSql(wrapper).contains("create_time ASC"), "最新 rule=1 应升序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("App 分页：DIY orderByType=0(综合) 固定 sort asc+create_time desc,不受 rule=1 影响")
    void appPage_diyComprehensiveFixed() {
        AskAppQuery query = new AskAppQuery();
        query.setOrderByType(0);
        query.setOrderByRule(1);
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        askService.appPage(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(2, wrapper.getExpression().getOrderBy().size(), "综合应 sort asc + create_time desc 两列");
        String full = fullSql(wrapper);
        assertTrue(full.contains("sort ASC") && full.contains("create_time DESC"),
                "综合应固定倒序不受规则影响: " + full);
    }

    @Test
    @DisplayName("App 详情：启用问答可读，访问量 SQL 原子自增，返回含 answer")
    void appDetail_visibleIncrementsAccessCount() {
        Ask ask = buildAsk(1L);
        doReturn(ask).when(askService).getById(1L);

        AskAppDetailVO vo = askService.appDetail(1L, null);

        assertNotNull(vo);
        assertEquals("每次2克，温水冲服", vo.getAnswer());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<UpdateWrapper<Ask>> captor = ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(askService).update(captor.capture());
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
    @DisplayName("App 详情：未启用且非提问人本人 → 视为不存在")
    void appDetail_pendingNotOwnerThrows() {
        Ask ask = buildAsk(1L);
        ask.setIsEnable(0);
        ask.setAskUserId(88L);
        doReturn(ask).when(askService).getById(1L);

        assertThrows(BizException.class, () -> askService.appDetail(1L, 99L));
        verify(askService, never()).update(any(Wrapper.class));
    }

    @Test
    @DisplayName("App 详情：未启用但为提问人本人 → 可读并自增")
    void appDetail_pendingOwnerVisible() {
        Ask ask = buildAsk(1L);
        ask.setIsEnable(0);
        ask.setAskUserId(88L);
        doReturn(ask).when(askService).getById(1L);

        AskAppDetailVO vo = askService.appDetail(1L, 88L);

        assertNotNull(vo);
        assertEquals("灵芝孢子粉", vo.getSpuName());
        verify(askService).update(any(Wrapper.class));
    }

    @Test
    @DisplayName("App 提问：登录会员提交后 is_enable=0、is_reply=0，记录 askUserId")
    void askByUser_createsPendingAsk() {
        AskAppAskDTO dto = new AskAppAskDTO();
        dto.setSpuId(10L);
        dto.setSpuName("灵芝孢子粉");
        dto.setSpuPic("https://cdn.example.com/spu10.png");
        dto.setTitle("怎么吃效果最好？");
        dto.setContent("饭前还是饭后");

        askService.askByUser(dto, 88L);

        ArgumentCaptor<Ask> captor = ArgumentCaptor.forClass(Ask.class);
        verify(askService).save(captor.capture());
        Ask saved = captor.getValue();
        assertEquals(0, saved.getIsEnable(), "用户提问默认不展示(is_enable=0)");
        assertEquals(0, saved.getIsReply(), "未回复");
        assertEquals(88L, saved.getAskUserId(), "应记录提问用户ID");
        assertEquals(0, saved.getAccessCount());
        assertEquals(0, saved.getSort());
        assertEquals(0, saved.getDeleted());
        assertEquals("怎么吃效果最好？", saved.getTitle());
        assertEquals("灵芝孢子粉", saved.getSpuName());
        assertNull(saved.getAskUser(), "token 无昵称，快照留空由前端兜底匿名");
    }

    @Test
    @DisplayName("App 提问：未登录拒绝")
    void askByUser_withoutLoginThrows() {
        AskAppAskDTO dto = new AskAppAskDTO();
        dto.setSpuId(10L);
        dto.setTitle("怎么吃效果最好？");

        assertThrows(BizException.class, () -> askService.askByUser(dto, null));
    }

    // 回复 / 启用确认

    @Test
    @DisplayName("运营回复：置 answer/is_reply=1/replyUser/replyTime，并自动启用展示")
    void replyAsk_setsReplyAndEnable() {
        Ask existing = buildAsk(1L);
        doReturn(existing).when(askService).getById(1L);
        AskReplyDTO dto = new AskReplyDTO();
        dto.setAnswer("建议每次2克");
        dto.setReplyUser("运营小二");

        askService.replyAsk(1L, dto);

        ArgumentCaptor<Ask> captor = ArgumentCaptor.forClass(Ask.class);
        verify(askService).updateById(captor.capture());
        Ask upd = captor.getValue();
        assertEquals("建议每次2克", upd.getAnswer());
        assertEquals(1, upd.getIsReply(), "回复后应置已回复");
        assertEquals("运营小二", upd.getReplyUser());
        assertEquals(1, upd.getIsEnable(), "回复即确认展示(is_enable=1)");
        assertNotNull(upd.getReplyTime(), "应记录回复时间");
    }

    @Test
    @DisplayName("运营回复：不存在问答拒绝")
    void replyAsk_missingThrows() {
        AskReplyDTO dto = new AskReplyDTO();
        dto.setAnswer("建议每次2克");

        assertThrows(BizException.class, () -> askService.replyAsk(99L, dto));
    }

    @Test
    @DisplayName("运营确认/停用：按入参置 is_enable")
    void enableAsk_setsEnable() {
        doReturn(buildAsk(1L)).when(askService).getById(1L);
        AskEnableDTO on = new AskEnableDTO();
        on.setIsEnable(1);
        AskEnableDTO off = new AskEnableDTO();
        off.setIsEnable(0);

        askService.enableAsk(1L, on);
        askService.enableAsk(1L, off);

        ArgumentCaptor<Ask> captor = ArgumentCaptor.forClass(Ask.class);
        verify(askService, Mockito.times(2)).updateById(captor.capture());
        assertEquals(1, captor.getAllValues().get(0).getIsEnable(), "确认展示置 is_enable=1");
        assertEquals(0, captor.getAllValues().get(1).getIsEnable(), "停用置 is_enable=0");
    }

    // 新增 / 编辑 / 删除

    @Test
    @DisplayName("管理端新增：默认启用并初始化计数")
    void createAsk_defaultsApplied() {
        AskSaveDTO dto = new AskSaveDTO();
        dto.setSpuId(10L);
        dto.setSpuName("灵芝孢子粉");
        dto.setTitle("能长期吃吗？");

        askService.createAsk(dto);

        ArgumentCaptor<Ask> captor = ArgumentCaptor.forClass(Ask.class);
        verify(askService).save(captor.capture());
        Ask saved = captor.getValue();
        assertEquals(1, saved.getIsEnable(), "管理端新增默认启用");
        assertEquals(0, saved.getIsReply());
        assertEquals(0, saved.getAccessCount());
        assertEquals(0, saved.getSort());
        assertEquals(0, saved.getDeleted());
    }

    @Test
    @DisplayName("管理端编辑：只更新入参字段，不触碰 answer/is_reply")
    void updateAsk_partialUpdate() {
        doReturn(buildAsk(1L)).when(askService).getById(1L);
        AskSaveDTO dto = new AskSaveDTO();
        dto.setId(1L);
        dto.setTitle("改后标题");

        askService.updateAsk(dto);

        ArgumentCaptor<Ask> captor = ArgumentCaptor.forClass(Ask.class);
        verify(askService).updateById(captor.capture());
        Ask upd = captor.getValue();
        assertEquals(1L, upd.getId());
        assertEquals("改后标题", upd.getTitle());
        assertNull(upd.getAnswer(), "编辑不应携带 answer");
        assertNull(upd.getIsReply(), "编辑不应携带 is_reply");
    }

    @Test
    @DisplayName("批量删除：逻辑删置 deleted=1")
    void deleteAsks_logicalDelete() {
        boolean ok = askService.deleteAsks(List.of(1L, 2L, 3L));

        assertTrue(ok);
        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).update(captor.capture());
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
    void deleteAsks_emptyIdsNoOp() {
        assertTrue(askService.deleteAsks(List.of()));
        verify(askService, never()).update(any(Wrapper.class));
    }

    // Feign

    @Test
    @DisplayName("Feign 分页：默认仅启用，支持 keyword/spuId")
    void pageFeign_enabledByDefault() {
        AskFeignPageQuery query = new AskFeignPageQuery();
        query.setKeyword("食用");
        query.setSpuId(10L);
        Ask ask = buildAsk(1L);
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of(ask));
        page.setTotal(1);
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        IPage<AskItemDTO> result = askService.pageFeign(query);

        assertEquals(1, result.getTotal());
        assertEquals("如何食用？", result.getRecords().get(0).getTitle());
        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertTrue(whereSql(wrapper).contains("is_enable = "), "Feign 默认应仅取启用");
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1), "is_enable=1 应入参");
    }

    @Test
    @DisplayName("Feign 分页：DIY orderByType=1(最新) rule=1(升序) → SQL 含 create_time ASC")
    void pageFeign_diyNewestAsc() {
        AskFeignPageQuery query = new AskFeignPageQuery();
        query.setOrderByType(1);
        query.setOrderByRule(1);
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        askService.pageFeign(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(1, wrapper.getExpression().getOrderBy().size(), "最新应只有 create_time 一列");
        assertTrue(fullSql(wrapper).contains("create_time ASC"), "最新 rule=1 应升序: " + fullSql(wrapper));
    }

    @Test
    @DisplayName("Feign 分页：DIY orderByType 缺省(0综合) → sort asc+create_time desc 固定")
    void pageFeign_diyDefaultComprehensive() {
        AskFeignPageQuery query = new AskFeignPageQuery();
        Page<Ask> page = new Page<>(1, 10);
        page.setRecords(List.of());
        doReturn(page).when(askService).page(any(Page.class), any(Wrapper.class));

        askService.pageFeign(query);

        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).page(any(Page.class), captor.capture());
        AbstractWrapper wrapper = captor.getValue();
        assertEquals(2, wrapper.getExpression().getOrderBy().size(), "综合应 sort asc + create_time desc 两列");
        String full = fullSql(wrapper);
        assertTrue(full.contains("sort ASC") && full.contains("create_time DESC"),
                "缺省应综合固定排序: " + full);
    }

    @Test
    @DisplayName("Feign 按 ids 回显：空 ids 直接返回空")
    void listByIdsFeign_emptyIds() {
        assertEquals(0, askService.listByIdsFeign(null).size());
        assertEquals(0, askService.listByIdsFeign(List.of()).size());
        verify(askService, never()).list(any(Wrapper.class));
    }

    @Test
    @DisplayName("Feign 按 ids 回显：仅启用未删，字段映射")
    void listByIdsFeign_enabledOnly() {
        Ask ask = buildAsk(1L);
        doReturn(List.of(ask)).when(askService).list(any(Wrapper.class));

        List<AskItemDTO> items = askService.listByIdsFeign(List.of(1L));

        assertEquals(1, items.size());
        assertEquals("如何食用？", items.get(0).getTitle());
        assertEquals("运营小二", items.get(0).getReplyUser());
        ArgumentCaptor<AbstractWrapper> captor = ArgumentCaptor.forClass(AbstractWrapper.class);
        verify(askService).list(captor.capture());
        String sql = whereSql(captor.getValue());
        assertTrue(sql.contains("is_enable = "), "回显应仅取启用: " + sql);
        assertTrue(sql.contains("deleted = "), "回显应过滤未删除: " + sql);
    }
}
