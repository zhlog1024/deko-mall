package com.deko.mall.cms.service.assemble;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.deko.mall.cms.service.ArticleService;
import com.deko.mall.cms.service.AskService;
import com.deko.mall.cms.service.VideoService;
import com.deko.mall.cms.api.dto.ArticleAutoQuery;
import com.deko.mall.cms.api.dto.ArticleItemDTO;
import com.deko.mall.cms.api.dto.AskFeignPageQuery;
import com.deko.mall.cms.api.dto.AskItemDTO;
import com.deko.mall.cms.api.dto.VideoFeignPageQuery;
import com.deko.mall.cms.api.dto.VideoItemDTO;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DIY 页面 App 端服务端装配服务
 * <p>
 * App 打开 DIY 页时，服务端遍历 config 组件，
 * 对 auto 模式(data_type=1)组件实时拉取最新数据并覆盖 data_auto_list（每次最新），
 * 指定模式(data_type=0)保留编辑器保存的 data_list 快照不动。
 * <p>
 * 覆盖组件：article-list/article-tabs、ask/ask-tabs、plugins-video/plugins-video-tabs；
 * goods-list/goods-tabs/goods-magic 因 App 端渲染器已具备 group_id 运行时兜底
 * （mall-app pages/diy/components/diy/goods-list.vue loadFilteredProducts），
 * 本期不在服务端装配（避免重复拉取），后续如需统一可在此按 SpuFeignClient 扩展。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiyAssembleService {

    private final AskService askService;
    private final VideoService videoService;
    private final ArticleService articleService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 装配 DIY 页面配置：遍历 diy_data/tabs_data 及各组件内嵌 tabs_list，
     * 对 auto 组件实时填充 data_auto_list。原地修改传入的 config 节点。
     *
     * @param config 解析后的页面 config（header/footer/diy_data/tabs_data）
     */
    public void assemble(ObjectNode config) {
        if (config == null) {
            return;
        }
        // 普通组件列表
        ArrayNode diyData = config.withArray("diy_data");
        if (diyData != null) {
            for (JsonNode item : diyData) {
                assembleComponent(item);
            }
        }
        // 选项卡组件列表（tabs 等，顶层内容在其 com_data.content.tabs_list 中按 tab 装配）
        ArrayNode tabsData = config.withArray("tabs_data");
        if (tabsData != null) {
            for (JsonNode item : tabsData) {
                assembleTabsRoot(item);
            }
        }
        // 其它组件内容（如 header/footer 内嵌业务组件，一般无 auto 数据，跳过）
    }

    /**
     * 装配普通组件节点（diy_data 元素）
     */
    private void assembleComponent(JsonNode item) {
        if (item == null || !item.isObject()) {
            return;
        }
        String key = item.path("key").asText("");
        ObjectNode content = (ObjectNode) item.path("com_data").path("content");
        if (content == null || content.isMissingNode()) {
            return;
        }
        // 组件内容自身是否含 auto 数据（article-list/ask/plugins-video 等）
        assembleByKey(key, content);
        // 组件内容含内嵌 tabs_list 的（goods-tabs/article-tabs/ask-tabs/plugins-video-tabs/tabs-magic/tabs-carousel/data-tabs 等）
        JsonNode tabsList = content.get("tabs_list");
        if (tabsList != null && tabsList.isArray()) {
            for (JsonNode tab : tabsList) {
                if (tab == null || !tab.isObject()) {
                    continue;
                }
                ObjectNode tabNode = (ObjectNode) tab;
                // 每 tab 继承父组件 key，筛选数据直接挂 tab 节点本身（对齐编辑器契约）
                assembleByKey(key, tabNode);
                // 兼容 tab 内嵌子内容结构
                assembleTabNode(tabNode);
            }
        }
    }

    /**
     * 选项卡根组件（tabs_data 顶层，tabs/tabs-carousel/tabs-magic/ask-tabs/plugins-video-tabs 等）
     */
    private void assembleTabsRoot(JsonNode item) {
        if (item == null || !item.isObject()) {
            return;
        }
        String rootKey = item.path("key").asText("");
        ObjectNode content = (ObjectNode) item.path("com_data").path("content");
        if (content == null || content.isMissingNode()) {
            return;
        }
        JsonNode tabsList = content.get("tabs_list");
        if (tabsList != null && tabsList.isArray()) {
            for (JsonNode tab : tabsList) {
                if (tab == null || !tab.isObject()) {
                    continue;
                }
                ObjectNode tabNode = (ObjectNode) tab;
                // 每 tab 继承父组件 key（如 ask-tabs/plugins-video-tabs），筛选字段在 tab 节点自身
                assembleByKey(rootKey, tabNode);
                // 兼容 tab 内嵌子内容结构
                assembleTabNode(tabNode);
            }
        }
    }

    /**
     * 单个 tab 节点的数据装配（tab 结构：含 data_type/data_auto_list 及可能内嵌
     * data_source_content 或各自内容字段——编辑器 tabs 版每 tab 独立 content）
     */
    private void assembleTabNode(ObjectNode tab) {
        // 情况1：tab 自身即组件内容（article-tabs/ask-tabs/plugins-video-tabs 每 tab 存
        // data_type/keywords/number/order_by_type/order_by_rule/data_auto_list 等）
        String tabType = tab.path("tabs_data_type").asText("");
        assembleByKey(tabType, tab);
        // 情况2：tab 内容在 goods_config/article_config 等子节点（data-tabs/goods-tabs 每 tab 结构）
        JsonNode goodsContentNode = tab.path("goods_config").path("content");
        if (goodsContentNode.isObject()) {
            assembleByKey("goods-tabs", (ObjectNode) goodsContentNode);
        }
        JsonNode articleContentNode = tab.path("article_config").path("content");
        if (articleContentNode.isObject()) {
            assembleByKey("article-tabs", (ObjectNode) articleContentNode);
        }
    }

    /**
     * 按组件 key 判断并装配（仅 auto 模式 data_type=1）
     */
    private void assembleByKey(String key, ObjectNode content) {
        if (content == null || content.isMissingNode() || key == null || key.isEmpty()) {
            return;
        }
        boolean isAuto = "1".equals(content.path("data_type").asText("0"));
        if (!isAuto) {
            return;
        }
        try {
            switch (key) {
                case "ask", "ask-tabs" -> fillAsk(content);
                case "plugins-video", "plugins-video-tabs" -> fillVideo(content);
                case "article-list", "article-tabs" -> fillArticle(content);
                // goods-list/goods-tabs/goods-magic：App 端已有运行时兜底，本期服务端不装配
                default -> {
                }
            }
        } catch (Exception e) {
            log.warn("DIY 服务端装配失败 key={}, 保持原快照", key, e);
        }
    }

    /**
     * 问答 auto 装配：按 content 筛选条件拉最新问答，覆盖 data_auto_list（渲染行结构对齐 ask 渲染器）
     */
    private void fillAsk(ObjectNode content) {
        int number = content.path("number").asInt(10);
        number = Math.min(Math.max(number, 1), 50);
        AskFeignPageQuery query = new AskFeignPageQuery();
        query.setPageNum(1);
        query.setPageSize(number);
        query.setKeyword(strOrNull(content, "keywords"));
        if (content.has("spu_id") && content.path("spu_id").asLong(0) > 0) {
            query.setSpuId(content.path("spu_id").asLong());
        }
        Integer isReply = intOrNull(content, "is_reply");
        if (isReply != null) {
            query.setIsReply(isReply);
        }
        query.setIsEnable(1);
        query.setOrderByType(content.path("order_by_type").asInt(0));
        query.setOrderByRule(content.path("order_by_rule").asInt(0));
        PageResult<AskItemDTO> pageResult = PageResult.success(askService.pageFeign(query));
        List<AskItemDTO> list = pageResult != null && pageResult.getData() != null
                && pageResult.getData().getList() != null ? pageResult.getData().getList() : new ArrayList<>();
        ArrayNode rows = objectMapper.createArrayNode();
        for (AskItemDTO dto : list) {
            ObjectNode row = objectMapper.createObjectNode();
            row.put("id", dto.getId());
            row.put("title", dto.getTitle() == null ? "" : dto.getTitle());
            row.put("is_reply", dto.getIsReply() == null ? 0 : dto.getIsReply());
            row.put("access_count", dto.getAccessCount() == null ? 0 : dto.getAccessCount());
            row.put("spu_id", dto.getSpuId() == null ? 0 : dto.getSpuId());
            if (dto.getSpuName() != null) {
                row.put("spu_name", dto.getSpuName());
            }
            if (dto.getSpuPic() != null) {
                row.put("spu_pic", dto.getSpuPic());
            }
            if (dto.getCreateTime() != null) {
                row.put("add_time_date", dto.getCreateTime().format(DATE_TIME_FMT));
            }
            row.put("url", "/pages/plugins/ask/detail/detail?id=" + dto.getId());
            rows.add(row);
        }
        content.set("data_auto_list", rows);
    }

    /**
     * 视频 auto 装配：渲染行结构对齐 plugins-video-list 渲染器（裸行）
     */
    private void fillVideo(ObjectNode content) {
        int number = content.path("number").asInt(10);
        number = Math.min(Math.max(number, 1), 50);
        VideoFeignPageQuery query = new VideoFeignPageQuery();
        query.setPageNum(1);
        query.setPageSize(number);
        query.setKeyword(strOrNull(content, "keywords"));
        Integer recommended = intOrNull(content, "is_recommended");
        if (recommended != null) {
            query.setIsRecommended(recommended);
        }
        Integer hot = intOrNull(content, "is_hot");
        if (hot != null) {
            query.setIsHot(hot);
        }
        query.setIsEnable(1);
        Integer orderByType = content.path("order_by_type").isMissingNode() ? null : content.path("order_by_type").asInt();
        // orderByType → sortField（0综合=null/1最新=createTime/2浏览=accessCount/3时长=duration）
        String sortField = null;
        if (orderByType != null) {
            sortField = switch (orderByType) {
                case 1 -> "createTime";
                case 2 -> "accessCount";
                case 3 -> "duration";
                default -> null;
            };
        }
        query.setSortField(sortField);
        query.setOrderByRule(content.path("order_by_rule").asInt(0));
        PageResult<VideoItemDTO> pageResult = PageResult.success(videoService.pageFeign(query));
        List<VideoItemDTO> list = pageResult != null && pageResult.getData() != null
                && pageResult.getData().getList() != null ? pageResult.getData().getList() : new ArrayList<>();
        ArrayNode rows = objectMapper.createArrayNode();
        for (VideoItemDTO dto : list) {
            ObjectNode row = objectMapper.createObjectNode();
            row.put("id", dto.getId());
            row.put("title", dto.getTitle() == null ? "" : dto.getTitle());
            if (dto.getCover() != null) {
                row.put("cover", dto.getCover());
            }
            if (dto.getVideoUrl() != null) {
                row.put("video_url", dto.getVideoUrl());
            }
            row.put("access_count", dto.getAccessCount() == null ? 0 : dto.getAccessCount());
            row.put("duration", dto.getDuration() == null ? 0 : dto.getDuration());
            if (dto.getCreateTime() != null) {
                row.put("add_time", dto.getCreateTime().format(DATE_TIME_FMT));
            }
            row.put("url", "/pages/plugins/video/detail/detail?id=" + dto.getId());
            rows.add(row);
        }
        content.set("data_auto_list", rows);
    }

    /**
     * 文章 auto 装配：渲染行结构对齐 article 渲染器（data_auto_list 裸行 + data 包裹由渲染端兼容）
     */
    private void fillArticle(ObjectNode content) {
        int number = content.path("number").asInt(10);
        number = Math.min(Math.max(number, 1), 50);
        ArticleAutoQuery query = new ArticleAutoQuery();
        query.setNumber(number);
        query.setKeyword(strOrNull(content, "keywords"));
        if (content.has("only_cover")) {
            query.setOnlyCover(content.path("only_cover").asInt(0));
        }
        List<Long> categoryIds = new ArrayList<>();
        JsonNode catNode = content.get("category_ids");
        if (catNode != null && catNode.isArray()) {
            for (JsonNode c : catNode) {
                long id = c.asLong(0);
                if (id > 0) {
                    categoryIds.add(id);
                }
            }
        }
        if (!categoryIds.isEmpty()) {
            query.setCategoryIds(categoryIds);
        }
        query.setOrderByType(content.path("order_by_type").isMissingNode() ? null : content.path("order_by_type").asInt());
        query.setOrderByRule(content.path("order_by_rule").asInt(0) == 0 ? "desc" : "asc");
        List<ArticleItemDTO> list = articleService.autoListFeign(query);
        if (list == null) {
            list = new ArrayList<>();
        }
        ArrayNode rows = objectMapper.createArrayNode();
        for (ArticleItemDTO dto : list) {
            ObjectNode data = objectMapper.createObjectNode();
            data.put("id", dto.getId());
            data.put("title", dto.getTitle() == null ? "" : dto.getTitle());
            if (dto.getCover() != null) {
                data.put("cover", dto.getCover());
            }
            if (dto.getAccessCount() != null) {
                data.put("access_count", dto.getAccessCount());
            }
            if (dto.getCreateTime() != null) {
                data.put("add_time_date", dto.getCreateTime().format(DATE_TIME_FMT));
            }
            data.put("url", "/pages/article-detail/article-detail?id=" + dto.getId());
            rows.add(data);
        }
        content.set("data_auto_list", rows);
    }

    private String strOrNull(ObjectNode content, String field) {
        JsonNode node = content.get(field);
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String v = node.asText("");
        return StringUtils.hasText(v) ? v : null;
    }

    private Integer intOrNull(ObjectNode content, String field) {
        JsonNode node = content.get(field);
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String v = node.asText("");
        if (!StringUtils.hasText(v)) {
            return null;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
