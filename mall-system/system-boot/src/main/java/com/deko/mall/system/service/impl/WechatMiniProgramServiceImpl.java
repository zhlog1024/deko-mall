package com.deko.mall.system.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.deko.mall.common.constant.RedisConstants;
import com.deko.mall.system.service.WechatMiniProgramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信小程序服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatMiniProgramServiceImpl implements WechatMiniProgramService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${wx.miniapp.appId}")
    private String appid;

    @Value("${wx.miniapp.appSecret}")
    private String secret;

    @Value("${wx.miniapp.original-id:}")
    private String originalId;

    // 微信接口基础 URL
    private static final String WECHAT_API_BASE = "https://api.weixin.qq.com";
    
    // 获取 access_token 的 URL
    private static final String ACCESS_TOKEN_URL = WECHAT_API_BASE + "/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    
    // 生成 URL Link 的 URL（非短链）
    private static final String GENERATE_URL_LINK = WECHAT_API_BASE + "/wxa/generate_url_link";
    
    // 生成短链的 URL
    private static final String GENERATE_SHORT_URL_LINK = WECHAT_API_BASE + "/wxa/gen_shorten_url_link";
    
    // 获取小程序二维码的 URL
    private static final String GET_QRCODE_URL = WECHAT_API_BASE + "/wxa/get_qrcode";

    /**
     * 获取微信小程序 access_token
     * 
     * @return access_token
     * @throws Exception 获取失败时抛出异常
     */
    private String getAccessToken() throws Exception {
        // 先从 Redis 中获取
        String cachedToken = redisTemplate.opsForValue().get(RedisConstants.WECHAT_ACCESS_TOKEN);
        if (cachedToken != null) {
            log.debug("从缓存中获取到 access_token: {}", cachedToken);
            return cachedToken;
        }
        
        // 从微信接口获取新的 access_token
        String url = String.format(ACCESS_TOKEN_URL, appid, secret);
        String response = httpGet(url);
        
        JSONObject jsonObject = JSONUtil.parseObj(response);
        if (jsonObject.containsKey("access_token")) {
            String accessToken = jsonObject.get("access_token", String.class);
            int expiresIn = jsonObject.getInt("expires_in", 7200);
            
            // 缓存到 Redis，提前 5 分钟过期
            redisTemplate.opsForValue().set(
                RedisConstants.WECHAT_ACCESS_TOKEN, 
                accessToken, 
                expiresIn - 300, 
                TimeUnit.SECONDS
            );
            
            log.info("成功获取并缓存 access_token: {}", accessToken);
            return accessToken;
        } else {
            log.error("获取 access_token 失败：{}", response);
            throw new Exception("获取 access_token 失败：" + jsonObject.getStr("errmsg"));
        }
    }

    @Override
    public Map<String, String> generateUrlLink(String path, String scene, String envVersion, boolean isShorten) throws Exception {
        log.info("开始生成微信小程序 URL Link, path={}, scene={}, envVersion={}, isShorten={}",
            path, scene, envVersion, isShorten);

        String accessToken = getAccessToken();
        String apiUrl = (isShorten ? GENERATE_SHORT_URL_LINK : GENERATE_URL_LINK) + "?access_token=" + accessToken;

        // 构建请求参数
        JSONObject requestParams = new JSONObject();

        if (isShorten) {
            // 短链需要放在 shorten_config 中
            JSONObject shortenConfig = new JSONObject();
            shortenConfig.set("page_path", path != null ? path : "pages/index/index");
            shortenConfig.set("query", scene);
            shortenConfig.set("is_edit", false);
            requestParams.set("shorten_config", shortenConfig);
        } else {
            // 长链参数
            requestParams.set("path", path != null ? path : "pages/index/index");
            requestParams.set("query", scene);
            requestParams.set("env_version", envVersion != null ? envVersion : "release");
        }

        log.debug("生成 URL Link 请求参数：{}", requestParams.toString());

        // 发送 POST 请求
        String response = httpPost(apiUrl, requestParams.toString());
        JSONObject jsonResponse = JSONUtil.parseObj(response);

        if (jsonResponse.containsKey("url_link") || jsonResponse.containsKey("shorten_url_link")) {
            Map<String, String> result = new HashMap<>();
            String urlLink = isShorten ?
                jsonResponse.getStr("shorten_url_link") :
                jsonResponse.getStr("url_link");
            result.put("url_link", urlLink);

            log.info("成功生成微信小程序 URL Link: {}", urlLink);
            return result;
        } else {
            log.error("生成 URL Link 失败：{}", response);
            throw new Exception("生成 URL Link 失败：" + jsonResponse.getStr("errmsg"));
        }
    }

    @Override
    public Map<String, String> getMiniProgramCode(String scene, String page, boolean checkPath, String envVersion) throws Exception {
        log.info("开始获取小程序二维码，scene={}, page={}, checkPath={}, envVersion={}",
            scene, page, checkPath, envVersion);

        String accessToken = getAccessToken();

        // 构建请求参数
        JSONObject requestParams = new JSONObject();
        requestParams.set("scene", scene);
        requestParams.set("page", page != null ? page : "pages/index/index");
        requestParams.set("check_path", checkPath);
        requestParams.set("env_version", envVersion != null ? envVersion : "release");

        log.debug("获取二维码请求参数：{}", requestParams.toString());

        // 发送 POST 请求获取二维码图片
        String apiUrl = GET_QRCODE_URL + "?access_token=" + accessToken;
        byte[] qrCodeBytes = httpPostBinary(apiUrl, requestParams.toString());

        // 将二进制数据转换为 base64
        String base64 = java.util.Base64.getEncoder().encodeToString(qrCodeBytes);

        Map<String, String> result = new HashMap<>();
        result.put("content_type", "image/jpeg");
        result.put("base64", base64);

        log.info("成功获取小程序二维码，大小：{} bytes", qrCodeBytes.length);
        return result;
    }

    @Override
    public Map<String, Object> getShareConfig(Long spuId, String shareId, String page) throws Exception {
        log.info("获取分享配置，spuId={}, shareId={}, page={}", spuId, shareId, page);

        // 构建scene参数
        String scene = "spuId=" + spuId;
        if (shareId != null && !shareId.isEmpty()) {
            scene += "&shareId=" + shareId;
        }

        String urlLink = null;
        try {
            // 尝试生成小程序URL Link
            urlLink = generateUrlLink(page, scene, "release", true)
                    .get("url_link");
        } catch (Exception e) {
            log.warn("生成URL Link失败，使用页面路径代替: {}", e.getMessage());
        }

        // 构建分享配置
        Map<String, Object> shareConfig = new HashMap<>();
        shareConfig.put("appId", appid);
        shareConfig.put("originalId", originalId);
        shareConfig.put("urlLink", urlLink);
        shareConfig.put("pagePath", page != null ? page : "pages/product/product");
        shareConfig.put("scene", scene);

        log.info("分享配置获取成功，urlLink={}", urlLink);
        return shareConfig;
    }

    /**
     * HTTP GET 请求
     */
    private String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        return response.toString();
    }

    /**
     * HTTP POST 请求（JSON）
     */
    private String httpPost(String urlStr, String jsonBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        
        try (java.io.OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        return response.toString();
    }

    /**
     * HTTP POST 请求（返回二进制数据）
     */
    private byte[] httpPostBinary(String urlStr, String jsonBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        
        try (java.io.OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 读取二进制响应
        try (java.io.InputStream is = conn.getInputStream()) {
            return is.readAllBytes();
        }
    }
}
