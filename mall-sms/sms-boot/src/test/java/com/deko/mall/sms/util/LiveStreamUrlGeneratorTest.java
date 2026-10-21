package com.deko.mall.sms.util;

import cn.hutool.crypto.digest.DigestUtil;
import com.deko.mall.sms.config.LiveProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 腾讯云直播推拉流地址生成器测试
 * <p>
 * 验证：推流地址含防盗链签名（txSecret = MD5(key + streamName + txTime)），
 * 拉流地址为 HLS 前缀（前端契约拼接 .m3u8）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LiveStreamUrlGeneratorTest {

    @Mock
    private LiveProperties liveProperties;

    @InjectMocks
    private LiveStreamUrlGenerator generator;

    @BeforeEach
    void setUp() {
        when(liveProperties.getPushDomain()).thenReturn("push.example.com");
        when(liveProperties.getPlayDomain()).thenReturn("play.example.com");
        when(liveProperties.getPushAuthKey()).thenReturn("testAuthKey");
        when(liveProperties.getPlayUseHttps()).thenReturn(true);
    }

    @Test
    @DisplayName("推流地址：rtmp 前缀 + 防盗链签名正确")
    void generatePushUrl_shouldSignWithAuthKey() {
        String streamName = "stream_test_001";
        String url = generator.generatePushUrl(streamName);

        assertTrue(url.startsWith("rtmp://push.example.com/live/" + streamName + "?txSecret="),
                "推流地址应以 rtmp 域名/应用/流名开头: " + url);
        assertTrue(url.contains("&txTime="), "推流地址应包含 txTime");
        assertTrue(url.contains("&txHost=push.example.com"), "推流地址应包含 txHost");

        // 校验 txSecret = MD5(鉴权Key + 流名 + txTime)
        String txTime = extractParam(url, "txTime");
        String expectedSecret = DigestUtil.md5Hex("testAuthKey" + streamName + txTime);
        assertEquals(expectedSecret, extractParam(url, "txSecret"));
    }

    @Test
    @DisplayName("拉流地址：https HLS 前缀，不含防盗链参数（前端拼 .m3u8）")
    void generatePullUrl_shouldReturnHlsPrefix() {
        String streamName = "stream_test_002";
        String url = generator.generatePullUrl(streamName);
        assertEquals("https://play.example.com/live/" + streamName, url);
        assertTrue(!url.contains("?"), "拉流地址不应含查询参数（前端契约会拼接 .m3u8）");
    }

    @Test
    @DisplayName("拉流地址：play-use-https=false 时使用 http")
    void generatePullUrl_shouldUseHttpWhenConfigured() {
        when(liveProperties.getPlayUseHttps()).thenReturn(false);
        String url = generator.generatePullUrl("stream_test_003");
        assertEquals("http://play.example.com/live/stream_test_003", url);
    }

    private String extractParam(String url, String key) {
        String query = url.substring(url.indexOf('?') + 1);
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv[0].equals(key)) {
                return kv[1];
            }
        }
        return "";
    }
}
