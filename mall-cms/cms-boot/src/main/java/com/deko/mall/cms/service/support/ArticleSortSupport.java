package com.deko.mall.cms.service.support;

/**
 * 文章列表排序纯逻辑支持（无 Spring 依赖，可单元测试）
 * <p>排序类型语义ConstService.common_article_order_by_type_list：
 * 0=综合(access_count,id) 1=人气(access_count) 2=最新(id)
 */
public final class ArticleSortSupport {

    private ArticleSortSupport() {
    }

    /**
     * 解析排序类型为排序列名（按优先级排列）
     *
     * @param orderByType 0综合/1人气/2最新，空或非法默认0
     * @return 排序列名数组
     */
    public static String[] resolveOrderColumns(Integer orderByType) {
        int type = orderByType == null ? 0 : orderByType;
        return switch (type) {
            case 1 -> new String[]{"access_count"};
            case 2 -> new String[]{"id"};
            default -> new String[]{"access_count", "id"};
        };
    }

    /**
     * 解析排序规则（asc/desc，非法默认 desc）
     *
     * @param rule 排序规则
     * @return "asc" 或 "desc"
     */
    public static String resolveRule(String rule) {
        return "asc".equalsIgnoreCase(rule) ? "asc" : "desc";
    }
}
