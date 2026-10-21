package com.deko.mall.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.cms.model.dto.DiyAppTabbarDataDTO;
import com.deko.mall.cms.model.dto.DiyAppTabbarSaveDTO;
import com.deko.mall.cms.model.query.DiyGoodsQuery;
import com.deko.mall.cms.model.query.DiyPageQuery;
import com.deko.mall.cms.model.vo.DiyApiDataVO;
import com.deko.mall.cms.model.vo.DiyApiInitVO;
import com.deko.mall.cms.model.vo.DiyGoodsItemVO;
import com.deko.mall.cms.model.vo.DiyPageListItemVO;
import com.deko.mall.cms.model.vo.DiyPageVO;
import com.deko.mall.cms.service.DiyApiService;
import com.deko.mall.cms.service.DiyPageService;
import com.deko.mall.pms.api.BrandFeignClient;
import com.deko.mall.pms.api.CategoryFeignClient;
import com.deko.mall.pms.api.SpuFeignClient;
import com.deko.mall.cms.service.ArticleCategoryService;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.cms.service.VideoService;
import com.deko.mall.cms.model.vo.ArticleCategoryVO;
import com.deko.mall.sms.api.LiveRoomFeignClient;
import com.deko.mall.sms.api.dto.LiveRoomItemDTO;
import com.deko.mall.cms.api.dto.ArticleAutoQuery;
import com.deko.mall.cms.api.dto.ArticleByIdsQuery;
import com.deko.mall.cms.api.dto.ArticleCategoryNodeDTO;
import com.deko.mall.cms.api.dto.ArticleItemDTO;
import com.deko.mall.cms.api.dto.AskByIdsQuery;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
import com.deko.mall.cms.api.dto.VideoByIdsQuery;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin端 - DIY 装修器 API 控制器
 * 用于装修器编辑时获取基础数据和选择数据
 */
@Tag(name = "Admin-DIY装修器API")
@RestController
@RequestMapping("/api/v1/diy-designer")
@Slf4j
@RequiredArgsConstructor
public class DiyApiController {

    private final DiyApiService diyApiService;
    private final DiyPageService diyPageService;
    private final BrandFeignClient brandFeignClient;
    private final CategoryFeignClient categoryFeignClient;
    private final SpuFeignClient spuFeignClient;
    private final ArticleService articleService;
    private final ArticleCategoryService articleCategoryService;
    private final AskService askService;
    private final VideoService videoService;
    private final LiveRoomFeignClient liveRoomFeignClient;

    @Operation(summary = "装修器初始化", description = "获取分类、品牌、商品分类等基础数据")
    @GetMapping("/init")
    public Result<DiyApiInitVO> init() {
        DiyApiInitVO data = diyApiService.init();
        return Result.success(data);
    }

    @Operation(summary = "App底部导航装修数据", description = "按业务标识读取 cms_diy_app_tabbar 装修配置，未配置返回 null")
    @PostMapping("/app-tabbar/data")
    public Result<Object> getAppTabbarData(@RequestBody(required = false) DiyAppTabbarDataDTO dto) {
        String business = dto == null ? null : dto.getBusiness();
        return Result.success(diyApiService.getAppTabbarData(business));
    }

    @Operation(summary = "App底部导航装修保存", description = "将导航配置序列化为 JSON，保存至 cms_diy_app_tabbar")
    @PostMapping("/app-tabbar/save")
    public Result<Boolean> saveAppTabbar(@RequestBody DiyAppTabbarSaveDTO dto) {
        diyApiService.saveAppTabbar(dto.getBusiness(), dto.getConfig());
        return Result.success(true);
    }

    @Operation(summary = "获取装修页面列表", description = "分页获取已启用的装修页面，供链接选择器使用")
    @PostMapping("/diy/list")
    public Result<DiyApiDataVO> getDiyPageList(@RequestBody DiyPageQuery query) {
        // 链接选择只允许指向已启用页面，避免装修出死链
        query.setIsEnabled(true);
        IPage<DiyPageVO> page = diyPageService.page(query);
        DiyApiDataVO vo = new DiyApiDataVO();
        vo.setDataList(page.getRecords().stream().map(item -> {
            DiyPageListItemVO listItem = new DiyPageListItemVO();
            listItem.setId(item.getId());
            listItem.setName(item.getName());
            listItem.setTitle(item.getName());
            listItem.setLogo(item.getCoverUrl());
            return listItem;
        }).toList());
        vo.setDataTotal(page.getTotal());
        vo.setPageTotal((int) page.getPages());
        vo.setPage((int) page.getCurrent());
        vo.setSize((int) page.getSize());
        return Result.success(vo);
    }

    @Operation(summary = "获取商品列表", description = "分页获取商品列表，用于装修器选择商品")
    @PostMapping("/goods/list")
    public Result<DiyApiDataVO> getGoodsList(@RequestBody DiyGoodsQuery query) {
        try {
            // 构建 PMS 模块的查询参数（使用下划线命名，与前端保持一致）
            Map<String, Object> params = new HashMap<>();
            params.put("page", query.getPage() != null ? query.getPage() : 1);
            params.put("page_size", query.getPageSize() != null ? query.getPageSize() : 30);
            if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
                params.put("keywords", query.getKeywords());
            }
            if (query.getBrandIds() != null && !query.getBrandIds().isEmpty()) {
                params.put("brand_ids", query.getBrandIds());
            }
            // 支持单分类查询，传递分类级别（使用下划线命名，与前端保持一致）
            if (query.getCategoryId() != null) {
                params.put("category_id", query.getCategoryId());
                if (query.getCategoryLevel() != null) {
                    params.put("category_level", query.getCategoryLevel());
                    log.info("🔍 分类查询 - category_id: {}, category_level: {}", query.getCategoryId(), query.getCategoryLevel());
                } else {
                    log.warn("⚠️ 分类查询缺少 category_level - category_id: {}", query.getCategoryId());
                }
            }
            if (query.getSpuType() != null && !query.getSpuType().isEmpty()) {
                params.put("spu_type", query.getSpuType());
            }
            // 只查询已上架商品
            params.put("status", 1);

            // 调用 PMS 模块 Feign 接口获取商品分页列表
            Result<Map<String, Object>> result = spuFeignClient.getSpuPage(params);

            DiyApiDataVO vo = new DiyApiDataVO();
            if (result != null && result.getData() != null) {
                Map<String, Object> data = result.getData();
                List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
                List<DiyGoodsItemVO> goodsList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(records)) {
                    for (Map<String, Object> record : records) {
                        DiyGoodsItemVO item = new DiyGoodsItemVO();
                        item.setId(getLongValue(record.get("id")));
                        item.setName(getStringValue(record.get("name")));
                        item.setPicUrl(getStringValue(record.get("picUrl")));
                        item.setPrice(getBigDecimalValue(record.get("price")));
                        item.setOriginPrice(getBigDecimalValue(record.get("originPrice")));
                        item.setSales(getIntValue(record.get("sales")));
                        item.setSpuType(getStringValue(record.get("spuType")));
                        item.setSpuTypeDesc(getStringValue(record.get("spuTypeDesc")));
                        item.setCategoryName(getStringValue(record.get("categoryName")));
                        item.setBrandName(getStringValue(record.get("brandName")));
                        item.setSimpleDesc(getStringValue(record.get("simpleDesc"))); // 添加商品简述字段
                        goodsList.add(item);
                    }
                }
                vo.setDataList(goodsList);
                vo.setDataTotal(getLongValue(data.get("total")));
                vo.setPageTotal(getIntValue(data.get("pages")));
                vo.setPage(getIntValue(data.get("current")));
                vo.setSize(getIntValue(data.get("size")));
            } else {
                vo.setDataList(new ArrayList<>());
                vo.setDataTotal(0L);
                vo.setPageTotal(0);
                vo.setPage(query.getPage() != null ? query.getPage() : 1);
                vo.setSize(query.getPageSize() != null ? query.getPageSize() : 30);
            }
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            DiyApiDataVO vo = new DiyApiDataVO();
            vo.setDataList(new ArrayList<>());
            vo.setDataTotal(0L);
            vo.setPageTotal(0);
            vo.setPage(query.getPage() != null ? query.getPage() : 1);
            vo.setSize(query.getPageSize() != null ? query.getPageSize() : 30);
            return Result.success(vo);
        }
    }

    @Operation(summary = "文章分类树", description = "启用分类树,供装修器文章组件分类选择器与预览")
    @GetMapping("/article/category")
    public Result<List<ArticleCategoryNodeDTO>> getArticleCategory() {
        return Result.success(convertCategoryTree(articleCategoryService.tree(true)));
    }

    @Operation(summary = "文章指定数据", description = "按 ids 拉取,供装修器文章组件指定数据模式")
    @PostMapping("/article/list")
    public Result<List<ArticleItemDTO>> getArticleList(@RequestBody ArticleByIdsQuery query) {
        return Result.success(articleService.listByIdsFeign(query == null ? null : query.getIds()));
    }

    @Operation(summary = "文章自动数据", description = "分类(含子级)+排序+数量,供装修器文章组件自动数据模式预览")
    @PostMapping("/article/auto-list")
    public Result<List<ArticleItemDTO>> getArticleAutoList(@RequestBody ArticleAutoQuery query) {
        return Result.success(articleService.autoListFeign(query));
    }

    @Operation(summary = "问答指定数据", description = "按 ids 拉取,供装修器问答组件指定数据模式回显")
    @GetMapping("/ask/list")
    public Result<List<AskItemDTO>> getAskList(@RequestParam(required = false) List<Long> ids) {
        AskByIdsQuery query = new AskByIdsQuery();
        query.setIds(ids);
        return Result.success(askService.listByIdsFeign(query == null ? null : query.getIds()));
    }

    @Operation(summary = "问答自动数据", description = "关键词/商品SPU/回复状态+数量+排序,供装修器问答组件自动数据模式预览;仅取启用(is_enable=1)问答")
    @GetMapping("/ask/auto-list")
    public Result<List<AskItemDTO>> getAskAutoList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long spuId,
            @RequestParam(required = false) Integer isReply,
            @RequestParam(required = false, defaultValue = "10") Integer number,
            @RequestParam(required = false) Integer orderByType,
            @RequestParam(required = false) String orderByRule) {
        // orderByType/orderByRule → AskFeignPageQuery 真实转发(与编辑器选项一致):
        // orderByType 0=综合(固定 sort asc+创建时间倒序)/1=最新(按创建时间,方向由规则决定);
        // 规则兼容编辑器入参 asc/desc 或数值 0/1,统一归一为 0=降序/1=升序
        AskFeignPageQuery query = new AskFeignPageQuery();
        query.setPageNum(1);
        query.setPageSize(number != null && number > 0 ? number : 10);
        query.setKeyword(keyword);
        query.setSpuId(spuId);
        query.setIsReply(isReply);
        query.setIsEnable(1);
        query.setOrderByType(orderByType != null ? orderByType : 0);
        query.setOrderByRule(resolveOrderByRule(orderByRule));
        PageResult<AskItemDTO> pageResult = PageResult.success(askService.pageFeign(query));
        List<AskItemDTO> list = pageResult != null && pageResult.getData() != null
                && pageResult.getData().getList() != null ? pageResult.getData().getList() : new ArrayList<>();
        return Result.success(list);
    }

    @Operation(summary = "视频指定数据", description = "按 ids 拉取,供装修器视频组件指定数据模式回显")
    @GetMapping("/plugins-video/list")
    public Result<List<VideoItemDTO>> getPluginsVideoList(@RequestParam(required = false) List<Long> ids) {
        VideoByIdsQuery query = new VideoByIdsQuery();
        query.setIds(ids);
        return Result.success(videoService.listByIdsFeign(query == null ? null : query.getIds()));
    }

    @Operation(summary = "直播自动数据", description = "关键词/多状态/推荐+数量,供装修器直播组件自动数据模式预览;封禁直播间不返回")
    @GetMapping("/live/auto-list")
    public Result<List<LiveRoomItemDTO>> getLiveAutoList(
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer isRecommended,
            @RequestParam(required = false, defaultValue = "10") Integer number) {
        return liveRoomFeignClient.diyList(keywords, status, isRecommended, number);
    }

    @Operation(summary = "视频自动数据", description = "关键词/推荐/热门+数量+排序,供装修器视频组件自动数据模式预览;仅取启用(is_enable=1)视频")
    @GetMapping("/plugins-video/auto-list")
    public Result<List<VideoItemDTO>> getPluginsVideoAutoList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isRecommended,
            @RequestParam(required = false) Integer isHot,
            @RequestParam(required = false, defaultValue = "10") Integer number,
            @RequestParam(required = false) Integer orderByType,
            @RequestParam(required = false) String orderByRule) {
        VideoFeignPageQuery query = new VideoFeignPageQuery();
        query.setPageNum(1);
        query.setPageSize(number != null && number > 0 ? number : 10);
        query.setKeyword(keyword);
        query.setIsRecommended(isRecommended);
        query.setIsHot(isHot);
        query.setIsEnable(1);
        // orderByType → sortField:0综合(默认 sort asc+创建时间倒序)/1最新(createTime)/2浏览(accessCount)/3时长(duration)
        String sortField = null;
        if (orderByType != null) {
            if (orderByType == 1) {
                sortField = "createTime";
            } else if (orderByType == 2) {
                sortField = "accessCount";
            } else if (orderByType == 3) {
                sortField = "duration";
            }
        }
        query.setSortField(sortField);
        // orderByRule → VideoFeignPageQuery.orderByRule 真实转发(0=降序/1=升序,对最新/浏览/时长生效,综合固定):
        // 规则兼容编辑器入参 asc/desc 或数值 0/1,统一归一为 0=降序/1=升序
        query.setOrderByRule(resolveOrderByRule(orderByRule));
        PageResult<VideoItemDTO> pageResult = PageResult.success(videoService.pageFeign(query));
        List<VideoItemDTO> list = pageResult != null && pageResult.getData() != null
                && pageResult.getData().getList() != null ? pageResult.getData().getList() : new ArrayList<>();
        return Result.success(list);
    }

    /** 工具方法：安全获取 Long 值 */
    private Long getLongValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try { return Long.parseLong(obj.toString()); } catch (Exception e) { return null; }
    }

    /** 工具方法：安全获取 Integer 值 */
    private Integer getIntValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }

    /** 工具方法：安全获取 BigDecimal 值 */
    private java.math.BigDecimal getBigDecimalValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.math.BigDecimal) return (java.math.BigDecimal) obj;
        if (obj instanceof Number) return new java.math.BigDecimal(obj.toString());
        try { return new java.math.BigDecimal(obj.toString()); } catch (Exception e) { return null; }
    }

    /** 工具方法：安全获取 String 值 */
    private String getStringValue(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    /**
     * DIY 排序规则归一：编辑器 content 存 order_by_rule 0=降序/1=升序，
     * 入参兼容 asc/desc 字符串与数值 "0"/"1"，统一归一为 0=降序/1=升序（缺省降序）
     */
    private Integer resolveOrderByRule(String orderByRule) {
        if (orderByRule != null && ("asc".equalsIgnoreCase(orderByRule) || "1".equals(orderByRule))) {
            return 1;
        }
        return 0;
    }

    /**
     * 将文章分类树转换为装修器所需字段。
     */
    private List<ArticleCategoryNodeDTO> convertCategoryTree(List<ArticleCategoryVO> tree) {
        if (tree == null) {
            return List.of();
        }
        return tree.stream().map(vo -> {
            ArticleCategoryNodeDTO dto = new ArticleCategoryNodeDTO();
            dto.setId(vo.getId());
            dto.setPid(vo.getPid());
            dto.setName(vo.getName());
            dto.setChildren(convertCategoryTree(vo.getChildren()));
            return dto;
        }).toList();
    }

}
