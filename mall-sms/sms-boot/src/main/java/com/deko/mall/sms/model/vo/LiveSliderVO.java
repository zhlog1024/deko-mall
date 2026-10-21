package com.deko.mall.sms.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播首页轮播项
 * <p>
 * 字段名严格对齐轮播组件 {@code components/slider/slider.vue} 的消费契约：
 * {@code item.images_url}（图片）、{@code item.event_value || item.url}（点击值）、
 * {@code item.event_type}（跳转类型，缺省按 1 处理）。
 * <p>
 * 不能直接复用 {@link BannerVO}（其字段为 title/imageUrl/redirectUrl，与组件不匹配，
 * 会导致图片取不到 onerror）。
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSliderVO {

    /**
     * 轮播图图片地址（组件读 images_url）
     */
    private String imagesUrl;

    /**
     * 点击跳转值（组件优先读 event_value，回退 url）
     */
    private String eventValue;

    /**
     * 跳转类型（组件缺省按 1 处理）
     */
    private Integer eventType;

    /**
     * 兜底跳转地址（组件在 event_value 为空时读 url）
     */
    private String url;
}
