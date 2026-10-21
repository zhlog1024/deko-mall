package com.deko.mall.sms.converter;

import com.deko.mall.sms.model.entity.SmsAdvert;
import com.deko.mall.sms.model.vo.BannerVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 广告位转换器字段映射测试
 * <p>
 * 守护 {@code picUrl -> imageUrl} 的改名映射：漏写时 MapStruct 不报错，
 * 只会运行期静默为 null（曾导致 app 首页横幅与直播首页轮播图全部无图）。
 */
class AdvertConverterTest {

    private final AdvertConverter converter = new AdvertConverterImpl();

    @Test
    @DisplayName("广告位转换：picUrl→imageUrl（title/redirectUrl 同名自动映射）")
    void entity2BannerVo_shouldMapPicUrlToImageUrl() {
        SmsAdvert entity = new SmsAdvert();
        entity.setTitle("首页大促");
        entity.setPicUrl("https://cdn.example.com/banner.png");
        entity.setRedirectUrl("/pages/activity/index");

        BannerVO vo = converter.entity2BannerVo(entity);

        assertNotNull(vo);
        assertEquals("首页大促", vo.getTitle());
        assertEquals("https://cdn.example.com/banner.png", vo.getImageUrl(), "picUrl 必须映射到 imageUrl");
        assertEquals("/pages/activity/index", vo.getRedirectUrl());
    }
}
