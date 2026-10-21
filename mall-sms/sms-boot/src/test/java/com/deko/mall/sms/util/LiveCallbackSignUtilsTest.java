package com.deko.mall.sms.util;

import cn.hutool.crypto.digest.DigestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 腾讯云直播回调签名工具测试
 * <p>
 * 覆盖官方算法 sign = MD5(key + t)、大小写容错、篡改检测、缺参兜底与时效判断
 */
class LiveCallbackSignUtilsTest {

    private static final String KEY = "test-callback-key";

    @Test
    @DisplayName("签名：严格按官方算法 sign = MD5(key + t)")
    void sign_shouldMatchOfficialAlgorithm() {
        long t = 1700000000L;
        assertTrue(LiveCallbackSignUtils.sign(KEY, t).equals(DigestUtil.md5Hex(KEY + t)));
    }

    @Test
    @DisplayName("校验：正确签名通过")
    void verify_shouldPassWithCorrectSign() {
        long t = System.currentTimeMillis() / 1000L + 600;
        assertTrue(LiveCallbackSignUtils.verify(KEY, t, LiveCallbackSignUtils.sign(KEY, t)));
    }

    @Test
    @DisplayName("校验：签名比对大小写不敏感")
    void verify_shouldIgnoreCase() {
        long t = System.currentTimeMillis() / 1000L + 600;
        String upper = LiveCallbackSignUtils.sign(KEY, t).toUpperCase();
        assertTrue(LiveCallbackSignUtils.verify(KEY, t, upper));
    }

    @Test
    @DisplayName("校验：篡改 t 后原签名失效（防重放/防篡改）")
    void verify_shouldFailWhenTimestampTampered() {
        long t = System.currentTimeMillis() / 1000L + 600;
        String sign = LiveCallbackSignUtils.sign(KEY, t);
        assertFalse(LiveCallbackSignUtils.verify(KEY, t + 1, sign));
    }

    @Test
    @DisplayName("校验：换 key 后原签名失效")
    void verify_shouldFailWithWrongKey() {
        long t = System.currentTimeMillis() / 1000L + 600;
        String sign = LiveCallbackSignUtils.sign(KEY, t);
        assertFalse(LiveCallbackSignUtils.verify("other-key", t, sign));
    }

    @Test
    @DisplayName("校验：key/t/sign 任一缺失即失败（不抛异常）")
    void verify_shouldFailOnBlankInput() {
        long t = System.currentTimeMillis() / 1000L + 600;
        String sign = LiveCallbackSignUtils.sign(KEY, t);
        assertFalse(LiveCallbackSignUtils.verify(null, t, sign));
        assertFalse(LiveCallbackSignUtils.verify("", t, sign));
        assertFalse(LiveCallbackSignUtils.verify(KEY, null, sign));
        assertFalse(LiveCallbackSignUtils.verify(KEY, t, null));
        assertFalse(LiveCallbackSignUtils.verify(KEY, t, "  "));
    }

    @Test
    @DisplayName("时效：t 为未来时间（有效期内）不算过期")
    void isExpired_shouldBeFalseWhenFresh() {
        long t = System.currentTimeMillis() / 1000L + 600;
        assertFalse(LiveCallbackSignUtils.isExpired(t));
    }

    @Test
    @DisplayName("时效：t 落在时钟偏差容忍窗口内不算过期")
    void isExpired_shouldTolerateClockSkew() {
        long t = System.currentTimeMillis() / 1000L - 30;
        assertFalse(LiveCallbackSignUtils.isExpired(t));
    }

    @Test
    @DisplayName("时效：t 超出有效期（含偏差）判为过期")
    void isExpired_shouldBeTrueWhenStale() {
        long t = System.currentTimeMillis() / 1000L - 700;
        assertTrue(LiveCallbackSignUtils.isExpired(t));
    }

    @Test
    @DisplayName("时效：t 缺失判为过期")
    void isExpired_shouldBeTrueWhenNull() {
        assertTrue(LiveCallbackSignUtils.isExpired(null));
    }
}
