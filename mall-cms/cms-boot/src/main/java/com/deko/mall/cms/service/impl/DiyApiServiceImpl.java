package com.deko.mall.cms.service.impl;

import com.deko.mall.cms.model.vo.*;
import com.deko.mall.cms.service.DiyApiService;
import com.deko.mall.cms.service.DiyAppTabbarService;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pms.api.BrandFeignClient;
import com.deko.mall.pms.api.CategoryFeignClient;
import com.deko.mall.pms.model.dto.PmsBrandDTO;
import com.deko.mall.pms.model.vo.CategoryVO;
import com.deko.mall.system.api.SysAppConfigFeignClient;
import com.deko.mall.system.api.SysAttachmentFeignClient;
import com.deko.mall.system.model.vo.SysAttachmentCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DIY 装修器 API 服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiyApiServiceImpl implements DiyApiService {

    private final BrandFeignClient brandFeignClient;
    private final CategoryFeignClient categoryFeignClient;
    private final SysAttachmentFeignClient sysAttachmentFeignClient;
    private final SysAppConfigFeignClient sysAppConfigFeignClient;
    private final DiyAppTabbarService diyAppTabbarService;

    /**
     * 装修器「预览」打开的地址（用户端 H5 装修页）。
     * <p>
     * 可用 Nacos mall-cms.yaml 的 diy.preview-url 覆盖，便于按环境指向不同 H5；
     * 默认值为用户端 H5 示例域名 m.example.com，可按部署环境自行调整。
     * <p>
     * 注意：字段初始化值保证脱离 Spring 容器（单元测试 new 实例）时也有正确默认值。
     */
    @Value("${diy.preview-url:https://m.example.com/#/pages/diy/diy}")
    private String previewUrl = "https://m.example.com/#/pages/diy/diy";

    @Override
    public DiyApiInitVO init() {
        DiyApiInitVO vo = new DiyApiInitVO();

        // 1. 配置数据
        vo.setConfig(buildConfig());

        // 2. 附件分类列表
        vo.setAttachmentCategory(getAttachmentCategoryList());

        // 3. 文章分类列表（暂时为空）
        vo.setArticleCategory(new ArrayList<>());

        // 4. 品牌分类列表（暂时为空）
        vo.setBrandCategory(new ArrayList<>());

        // 5. 品牌列表
        vo.setBrandList(getBrandList());

        // 6. 商品分类列表
        vo.setGoodsCategory(getGoodsCategoryList());

        // 7. 页面链接列表
        vo.setPageLinkList(getPageLinkList());

        // 8. 模块组件列表
        vo.setModuleList(getModuleList());

        // 9. 排序类型列表
        vo.setBrandOrderByTypeList(getBrandOrderByTypeList());
        vo.setArticleOrderByTypeList(getArticleOrderByTypeList());
        vo.setGoodsOrderByTypeList(getGoodsOrderByTypeList());
        vo.setDataOrderByRuleList(getDataOrderByRuleList());

        // 10. 插件列表
        vo.setPlugins(new ArrayList<>());

        return vo;
    }

    /**
     * 构建配置数据
     */
    private DiyConfigVO buildConfig() {
        DiyConfigVO config = new DiyConfigVO();
        config.setSiteName(getConfigValue("site.name", "Deko Mall"));
        config.setSiteLogo(getConfigValue("site.logo", ""));
        config.setCurrencySymbol(getConfigValue("site.currency.symbol", "¥"));
        config.setAttachmentHost(getConfigValue("file.attachment.host", "https://example.oss-cn-hangzhou.aliyuncs.com"));
        config.setDiyDetailUrl("/mall-cms/api/v1/diy-pages");
        config.setDiySaveUrl("/mall-cms/api/v1/diy-pages");
        // 底部导航装修数据/保存接口
        config.setAppTabbarDataUrl("/mall-cms/api/v1/diy-designer/app-tabbar/data");
        config.setAppTabbarSaveUrl("/mall-cms/api/v1/diy-designer/app-tabbar/save");
        config.setPreviewUrl(previewUrl);
        config.setDiyConfigOperate(buildDiyConfigOperate());
        config.setAttachmentOperate(buildAttachmentOperate());
        config.setAttachmentCategoryOperate(buildAttachmentCategoryOperate());
        return config;
    }

    /**
     * 构建装修器操作权限配置
     */
    private DiyConfigOperateVO buildDiyConfigOperate() {
        DiyConfigOperateVO operate = new DiyConfigOperateVO();
        operate.setIsBaseData(1);
        operate.setIsUploadAdmin(1);
        operate.setIsSaveButton(1);
        operate.setIsSaveCloseButton(1);
        return operate;
    }

    /**
     * 构建附件权限配置
     */
    private AttachmentOperateVO buildAttachmentOperate() {
        AttachmentOperateVO operate = new AttachmentOperateVO();
        operate.setIsUpload(1);
        operate.setIsDel(1);
        operate.setIsMove(1);
        operate.setIsEdit(1);
        return operate;
    }

    /**
     * 构建附件分类权限配置
     */
    private AttachmentCategoryOperateVO buildAttachmentCategoryOperate() {
        AttachmentCategoryOperateVO operate = new AttachmentCategoryOperateVO();
        operate.setIsAdd(1);
        operate.setIsEdit(1);
        operate.setIsDel(1);
        return operate;
    }

    /**
     * 读取系统配置；配置为空或远程调用失败时使用默认值。
     */
    private String getConfigValue(String key, String defaultValue) {
        try {
            Result<String> result = sysAppConfigFeignClient.getConfigValue(key);
            if (Result.isSuccess(result) && StringUtils.hasText(result.getData())) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("读取系统配置[{}]失败，回退默认值：{}", key, e.getMessage());
        }
        return defaultValue;
    }

    /**
     * 获取附件分类列表
     */
    private List<CategoryTreeVO> getAttachmentCategoryList() {
        try {
            Result<List<SysAttachmentCategoryVO>> result = sysAttachmentFeignClient.getCategoryTree();
            if (result != null && ResultCode.SUCCESS.getCode().equals(result.getCode()) && result.getData() != null) {
                return result.getData().stream()
                        .map(this::convertSysCategoryToCategoryTree)
                        .toList();
            }
        } catch (Exception e) {
            log.error("获取附件分类列表失败", e);
        }
        return new ArrayList<>();
    }

    /**
     * 将系统分类VO转换为DIY分类树VO
     */
    private CategoryTreeVO convertSysCategoryToCategoryTree(SysAttachmentCategoryVO category) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setLevel(0); // 系统分类没有level字段
        if (!CollectionUtils.isEmpty(category.getChildren())) {
            vo.setChildren(category.getChildren().stream()
                    .map(this::convertSysCategoryToCategoryTree)
                    .toList());
        }
        return vo;
    }

    /**
     * 获取页面链接列表
     */
    private List<PageLinkVO> getPageLinkList() {
        List<PageLinkVO> list = new ArrayList<>();

        // 1. 商城链接（带嵌套数据）
        PageLinkVO shopLink = new PageLinkVO();
        shopLink.setName("商城链接");
        shopLink.setType("shop");
        shopLink.setIsShow(1);
        shopLink.setUrl(null);
        shopLink.setData(getShopLinkData());
        list.add(shopLink);

        // 2. 商品分类
        PageLinkVO goodsCategoryLink = new PageLinkVO();
        goodsCategoryLink.setName("商品分类");
        goodsCategoryLink.setType("goods-category");
        goodsCategoryLink.setIsShow(1);
        goodsCategoryLink.setUrl(null);
        goodsCategoryLink.setData(null);
        list.add(goodsCategoryLink);

        // 3. 商品搜索
        PageLinkVO goodsSearchLink = new PageLinkVO();
        goodsSearchLink.setName("商品搜索");
        goodsSearchLink.setType("goods-search");
        goodsSearchLink.setIsShow(1);
        goodsSearchLink.setUrl(null);
        goodsSearchLink.setData(null);
        list.add(goodsSearchLink);

        // 4. 商品页面
        PageLinkVO goodsLink = new PageLinkVO();
        goodsLink.setName("商品页面");
        goodsLink.setType("goods");
        goodsLink.setIsShow(1);
        goodsLink.setUrl("/mall-cms/api/v1/diy-designer/goods/list");
        goodsLink.setData(null);
        list.add(goodsLink);

        // 5. DIY页面
        PageLinkVO diyLink = new PageLinkVO();
        diyLink.setName("DIY页面");
        diyLink.setType("diy");
        diyLink.setIsShow(1);
        diyLink.setUrl("/mall-cms/api/v1/diy-designer/diy/list");
        diyLink.setData(null);
        list.add(diyLink);

        // 6. 店铺（多商户）
        PageLinkVO merchantLink = new PageLinkVO();
        merchantLink.setName("店铺");
        merchantLink.setType("merchant");
        merchantLink.setIsShow(1);
        merchantLink.setUrl("/mall-pms/api/v1/shops/page");
        merchantLink.setData(null);
        list.add(merchantLink);

        // 门店链接：本仓未包含 mall-store / O2O，不开放

        // 6. 文章页面
        PageLinkVO articleLink = new PageLinkVO();
        articleLink.setName("文章页面");
        articleLink.setType("article");
        articleLink.setIsShow(1);
        articleLink.setUrl("/mall-cms/api/v1/article/page");
        articleLink.setData(null);
        list.add(articleLink);

        // 6.1 问答页面
        PageLinkVO askLink = new PageLinkVO();
        askLink.setName("问答页面");
        askLink.setType("ask");
        askLink.setIsShow(1);
        askLink.setUrl("/mall-cms/api/v1/ask/page");
        askLink.setData(null);
        list.add(askLink);

        // 6.2 视频页面
        PageLinkVO pluginsVideoLink = new PageLinkVO();
        pluginsVideoLink.setName("视频页面");
        pluginsVideoLink.setType("plugins-video");
        pluginsVideoLink.setIsShow(1);
        pluginsVideoLink.setUrl("/mall-cms/api/v1/video/page");
        pluginsVideoLink.setData(null);
        list.add(pluginsVideoLink);

        // 7. 页面设计（需要实现 design 页面路由）
        /*
        PageLinkVO designLink = new PageLinkVO();
        designLink.setName("页面设计");
        designLink.setType("design");
        designLink.setIsShow(1);
        designLink.setUrl("/mall-cms/api/v1/diy-designer/design/list");
        designLink.setData(null);
        list.add(designLink);
        */

        // 8. 自定义页面
        PageLinkVO customViewLink = new PageLinkVO();
        customViewLink.setName("自定义页面");
        customViewLink.setType("custom-view");
        customViewLink.setIsShow(1);
        customViewLink.setUrl("/mall-cms/api/v1/custom-view/page");
        customViewLink.setData(null);
        list.add(customViewLink);

        // 9. 品牌（需要实现品牌列表接口和前端页面）
        /*
        PageLinkVO brandLink = new PageLinkVO();
        brandLink.setName("品牌");
        brandLink.setType("brand");
        brandLink.setIsShow(1);
        brandLink.setUrl("/mall-cms/api/v1/diy-designer/brand/list");
        brandLink.setData(null);
        list.add(brandLink);
        */

        // 10. 自定义链接（前端特殊处理，不需要接口）
        PageLinkVO customUrlLink = new PageLinkVO();
        customUrlLink.setName("自定义链接");
        customUrlLink.setType("custom-url");
        customUrlLink.setIsShow(1);
        customUrlLink.setUrl(null);
        customUrlLink.setData(null);
        list.add(customUrlLink);

        // 11. 插件（需要实现插件系统）
        /*
        PageLinkVO pluginsLink = new PageLinkVO();
        pluginsLink.setName("插件");
        pluginsLink.setType("plugins");
        pluginsLink.setIsShow(1);
        pluginsLink.setUrl(null);
        pluginsLink.setData(new ArrayList<>());
        list.add(pluginsLink);
        */

        return list;
    }

    /**
     * 获取商城链接的嵌套数据
     */
    private List<Map<String, Object>> getShopLinkData() {
        List<Map<String, Object>> shopData = new ArrayList<>();

        // 基础链接
        Map<String, Object> baseGroup = new HashMap<>();
        baseGroup.put("name", "基础链接");
        baseGroup.put("type", "base");
        baseGroup.put("data", getBaseLinks());
        shopData.add(baseGroup);

        // 用户中心
        Map<String, Object> userGroup = new HashMap<>();
        userGroup.put("name", "用户中心");
        userGroup.put("type", "user");
        userGroup.put("data", getUserCenterLinks());
        shopData.add(userGroup);

        return shopData;
    }

    /**
     * 获取基础链接列表
     */
    private List<Map<String, String>> getBaseLinks() {
        List<Map<String, String>> links = new ArrayList<>();
        links.add(createSimpleLink("商城首页", "/pages/index/index"));
        links.add(createSimpleLink("商品分类", "/pages/goods-category/goods-category"));
        links.add(createSimpleLink("商品搜索", "/pages/goods-search/goods-search"));

        links.add(createSimpleLink("文章列表", "/pages/article-category/article-category"));
        
        
        return links;
    }

    /**
     * 获取用户中心链接列表
     */
    private List<Map<String, String>> getUserCenterLinks() {
        List<Map<String, String>> links = new ArrayList<>();
        links.add(createSimpleLink("用户中心", "/pages/user/user"));
        
        links.add(createSimpleLink("我的消息", "/pages/message/message"));
        
        
        return links;
    }

    /**
     * 创建简单链接
     */
    private Map<String, String> createSimpleLink(String name, String page) {
        Map<String, String> link = new HashMap<>();
        link.put("name", name);
        link.put("page", page);
        return link;
    }

    /**
     * 获取品牌列表
     */
    private List<BrandVO> getBrandList() {
        try {
            Result<List<PmsBrandDTO>> result = brandFeignClient.getBrandList();
            if (result != null && ResultCode.SUCCESS.getCode().equals(result.getCode()) && !CollectionUtils.isEmpty(result.getData())) {
                return result.getData().stream()
                        .map(brand -> {
                            BrandVO vo = new BrandVO();
                            vo.setId(brand.getId());
                            vo.setName(brand.getName());
                            vo.setLogoUrl(brand.getLogoUrl());
                            return vo;
                        })
                        .toList();
            }
        } catch (Exception e) {
            log.error("获取品牌列表失败", e);
        }
        return new ArrayList<>();
    }

    /**
     * 获取商品分类列表
     */
    private List<CategoryTreeVO> getGoodsCategoryList() {
        try {
            Result<List<CategoryVO>> result = categoryFeignClient.getAllCategories();
            if (result != null && ResultCode.SUCCESS.getCode().equals(result.getCode()) && !CollectionUtils.isEmpty(result.getData())) {
                return convertCategoryToTree(result.getData());
            }
        } catch (Exception e) {
            log.error("获取商品分类列表失败", e);
        }
        return new ArrayList<>();
    }

    /**
     * 转换分类VO为树形结构
     */
    private List<CategoryTreeVO> convertCategoryToTree(List<CategoryVO> categories) {
        return categories.stream()
                .map(category -> {
                    CategoryTreeVO vo = new CategoryTreeVO();
                    vo.setId(category.getId());
                    vo.setName(category.getName());
                    vo.setParentId(category.getParentId());
                    vo.setLevel(category.getLevel());
                    if (!CollectionUtils.isEmpty(category.getChildren())) {
                        vo.setChildren(convertCategoryToTree(category.getChildren()));
                    }
                    return vo;
                })
                .toList();
    }

    /**
     * 获取模块组件列表
     */
    private List<ModuleGroupVO> getModuleList() {
        List<ModuleGroupVO> list = new ArrayList<>();

        // 基础组件
        ModuleGroupVO baseModule = new ModuleGroupVO();
        baseModule.setName("基础组件");
        baseModule.setKey("base");
        baseModule.setData(getBaseModules());
        list.add(baseModule);

        // 插件（门店/店铺/直播等业务组件，前端分组展示名统一为"高级"）
        ModuleGroupVO pluginsModule = new ModuleGroupVO();
        pluginsModule.setName("高级");
        pluginsModule.setKey("plugins");
        pluginsModule.setData(getPluginsModules());
        list.add(pluginsModule);

        // 工具组件
        ModuleGroupVO toolModule = new ModuleGroupVO();
        toolModule.setName("工具组件");
        toolModule.setKey("tool");
        toolModule.setData(getToolModules());
        list.add(toolModule);

        return list;
    }

    /**
     * 获取基础组件列表
     */
    private List<ModuleVO> getBaseModules() {
        return List.of(
                createModule("tabs", "选项卡"),
                createModule("tabs-carousel", "选项卡轮播"),
                createModule("tabs-magic", "选项卡魔方"),
                createModule("carousel", "轮播图"),
                createModule("search", "搜索框"),
                createModule("nav-group", "导航组"),
                createModule("video", "视频"),
                createModule("goods-list", "商品列表"),
                createModule("goods-magic", "商品魔方"),
                createModule("goods-tabs", "商品选项卡"),
                createModule("article-list", "文章列表"),
                createModule("article-tabs", "文章选项卡"),
                createModule("ask", "问答"),
                createModule("ask-tabs", "问答选项卡"),
                createModule("plugins-video", "视频"),
                createModule("plugins-video-tabs", "视频选项卡"),
                createModule("notice", "公告"),
                createModule("custom", "自定义"),
                createModule("img-magic", "图片魔方"),
                createModule("data-magic", "数据魔方"),
                createModule("data-tabs", "数据选项卡"),
                createModule("hot-zone", "热区"),
                createModule("user-info", "用户信息")
        );
    }

    /**
     * 获取工具组件列表
     */
    private List<ModuleVO> getToolModules() {
        return List.of(
                createModule("title", "标题"),
                createModule("float-window", "悬浮按钮"),
                createModule("auxiliary-blank", "辅助空白"),
                createModule("row-line", "横线"),
                createModule("rich-text", "富文本")
        );
    }

    private ModuleVO createModule(String key, String name) {
        ModuleVO module = new ModuleVO();
        module.setKey(key);
        module.setName(name);
        return module;
    }

    /**
     * 获取插件组件列表
     * <p>
     * 开放：店铺(shop)、直播列表/选项卡。
     * 门店组件依赖独立门店服务，本仓未包含，不放入组件面板。
     */
    private List<ModuleVO> getPluginsModules() {
        return List.of(
                createModule("shop", "店铺"),
                createModule("plugins-live", "直播列表"),
                createModule("plugins-live-tabs", "直播选项卡")
        );
    }

    /**
     * 获取品牌排序类型列表
     */
    private List<OrderByTypeVO> getBrandOrderByTypeList() {
        return List.of(
                createOrderByType("default", "默认"),
                createOrderByType("sales", "销量"),
                createOrderByType("price", "价格")
        );
    }

    /**
     * 获取文章排序类型列表
     */
    private List<OrderByTypeVO> getArticleOrderByTypeList() {
        return List.of(
                createOrderByType("default", "默认"),
                createOrderByType("time", "时间")
        );
    }

    /**
     * 获取商品排序类型列表
     */
    private List<OrderByTypeVO> getGoodsOrderByTypeList() {
        return List.of(
                createOrderByType("default", "默认"),
                createOrderByType("sales", "销量"),
                createOrderByType("price", "价格"),
                createOrderByType("time", "时间")
        );
    }

    /**
     * 获取数据排序规则列表
     */
    private List<OrderByTypeVO> getDataOrderByRuleList() {
        return List.of(
                createOrderByType("asc", "升序"),
                createOrderByType("desc", "降序")
        );
    }

    private OrderByTypeVO createOrderByType(String value, String name) {
        OrderByTypeVO type = new OrderByTypeVO();
        type.setValue(value);
        type.setName(name);
        return type;
    }

    // #region App底部导航（tabbar）装修数据读写

    @Override
    public Object getAppTabbarData(String business) {
        return diyAppTabbarService.getTabbarConfig(business);
    }

    @Override
    public void saveAppTabbar(String business, Object config) {
        diyAppTabbarService.saveTabbarConfig(business, config);
    }
    //#endregion

}
