package com.deko.mall.oms.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OmsOrderMapper.xml 结构回归测试
 * <p>
 * 背景一：商家端订单列表报 "Unknown column 'oi2.supplier_id' in 'where clause'"
 * —— getOrderPage/listOrderPages 的 WHERE 直接引用 oi2.supplier_id，
 *    但 oms_order_item oi2 从未被 JOIN（该别名只存在于 getOrderPriceSum 的 FROM 中）。
 * 契约：涉及 oms_order_item 的供应商过滤必须使用 EXISTS 子查询关联 t1.id，
 *       禁止在外层 WHERE 直接引用未 JOIN 的别名。
 * <p>
 * 背景二：订单报价汇总报 "1054 - Unknown column 'oi.payment_amount' in 'field list'"
 * —— 仓库 DDL（deploy/db/schema/demo_mall.sql）里 oms_order_item 有 payment_amount，
 *   但线上库没有该列（DDL 与线上库漂移）；且多商品行 JOIN oms_order 会把订单级实付重复计入。
 * 契约：SQL 一律不引用 oms_order_item 的 payment_amount，实付只取订单级 oms_order.payment_amount。
 * <p>
 * 两处缺陷都属于同一类问题：XML 里的列引用与真实库表结构不一致，编译期不报、运行期 1054。
 */
class OmsOrderMapperXmlTest {

    /** 存在该缺陷的 select 列表（FROM 中无 oms_order_item 别名） */
    private static final String[] BROKEN_JOIN_SELECTS = {"getOrderPage", "listOrderPages"};

    private static final Pattern SELECT_BLOCK = Pattern.compile(
            "<select id=\"" + "([^\"]+)" + "\".*?</select>", Pattern.DOTALL);

    private static final Pattern SUPPLIER_ID_IF_BLOCK = Pattern.compile(
            "<if test='queryParams\\.supplierId !=null'>.*?</if>", Pattern.DOTALL);

    /** 订单商品行别名（oi / oi2 / i / t2 等）直接引用 payment_amount —— 线上库无此列 */
    private static final Pattern ITEM_PAYMENT_AMOUNT = Pattern.compile(
            "\\b(oi|oi2|i|t2)\\.payment_amount\\b", Pattern.CASE_INSENSITIVE);

    private String readXml() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/mapper/OmsOrderMapper.xml")) {
            assertNotNull(in, "未找到 /mapper/OmsOrderMapper.xml，请确认测试资源包含 mapper XML");
            String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            // 空白归一，避免换行导致字符串匹配失败
            return xml.replaceAll("\\s+", " ");
        }
    }

    private Document parseXml() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/mapper/OmsOrderMapper.xml")) {
            assertNotNull(in, "未找到 /mapper/OmsOrderMapper.xml，请确认测试资源包含 mapper XML");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // mapper XML 头部声明了 mybatis DTD，禁用外部实体解析，避免离线环境拉网络
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(in);
        }
    }

    private String extractSelect(String xml, String selectId) {
        Matcher m = SELECT_BLOCK.matcher(xml);
        while (m.find()) {
            if (selectId.equals(m.group(1))) {
                return m.group(0);
            }
        }
        throw new IllegalStateException("XML 中未找到 select id=" + selectId);
    }

    @Test
    @DisplayName("getOrderPage/listOrderPages 的供应商过滤必须通过 oms_order_item 子查询，禁止引用未 JOIN 的 oi2 别名")
    void supplierFilter_mustUseExistsSubquery() throws Exception {
        String xml = readXml();
        for (String selectId : BROKEN_JOIN_SELECTS) {
            String select = extractSelect(xml, selectId);

            Matcher m = SUPPLIER_ID_IF_BLOCK.matcher(select);
            assertTrue(m.find(), selectId + " 缺少 queryParams.supplierId 过滤条件");

            String ifBlock = m.group(0);
            // 供应商过滤必须为 EXISTS 子查询形式，且子查询必须关联 t1.id
            assertTrue(ifBlock.contains("SELECT 1 FROM oms_order_item oi2"),
                    selectId + " 的 supplierId 过滤未通过 oms_order_item 子查询：\n" + ifBlock);
            assertTrue(ifBlock.contains("oi2.order_id = t1.id"),
                    selectId + " 的 supplierId 子查询缺少 oi2.order_id = t1.id 关联：\n" + ifBlock);
            // 禁止在外层 WHERE 直接引用未 JOIN 的 oi2 别名（形如 AND oi2.supplier_id = #{...}）
            String inner = ifBlock.replaceFirst("^<if[^>]*>", "").replaceFirst("</if>\\s*$", "").trim();
            assertTrue(!inner.startsWith("AND oi2.supplier_id"),
                    selectId + " 在无 JOIN 的查询中直接引用了 oi2.supplier_id：\n" + ifBlock);
        }
    }

    @Test
    @DisplayName("getOrderPriceSum 通过 oms_order_item JOIN 过滤，oi2 别名合法")
    void priceSum_mayReferenceOi2Alias() throws Exception {
        String xml = readXml();
        String select = extractSelect(xml, "getOrderPriceSum");
        // oms_order_item oi2 已 JOIN，直接引用 oi2.supplier_id 合法（作为对照契约）
        assertTrue(select.contains("JOIN oms_order_item oi2"),
                "getOrderPriceSum 应 JOIN oms_order_item oi2 以支持商品/供应商维度过滤");
    }

    @Test
    @DisplayName("OmsOrderMapper.xml 必须是合法 XML（编译期不报，运行期才炸）")
    void mapperXml_mustBeWellFormed() throws Exception {
        Document doc = parseXml();
        assertNotNull(doc.getDocumentElement(), "OmsOrderMapper.xml 解析后没有根节点");
    }

    @Test
    @DisplayName("禁止引用 oms_order_item.payment_amount（线上库无此列，DDL 与线上漂移）")
    void itemPaymentAmount_isNotAColumn() throws Exception {
        String xml = readXml();
        Matcher m = SELECT_BLOCK.matcher(xml);
        while (m.find()) {
            assertFalse(ITEM_PAYMENT_AMOUNT.matcher(m.group(0)).find(),
                    m.group(1) + " 引用了 oms_order_item.payment_amount：线上库无该列，"
                            + "实付只能取订单级 oms_order.payment_amount（1054 回归）");
        }
    }

    @Test
    @DisplayName("getOrderPriceSum 的实付取订单级 payment_amount，不得再写成商品行 payment_amount")
    void priceSum_paymentAmount_mustComeFromOrder() throws Exception {
        String xml = readXml();
        String select = extractSelect(xml, "getOrderPriceSum");
        assertFalse(ITEM_PAYMENT_AMOUNT.matcher(select).find(),
                "getOrderPriceSum 仍引用商品行 payment_amount：\n" + select);
        assertTrue(select.contains("t1.payment_amount"),
                "getOrderPriceSum 的实付必须取自订单级 t1.payment_amount：\n" + select);
        assertTrue(select.contains("oms_order_item oi2"),
                "getOrderPriceSum 仍应按商品行过滤（供应商/商品条件）");
    }
}
