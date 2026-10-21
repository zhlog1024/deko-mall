package com.deko.mall.system.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.model.bo.MultipartFileBO;
import com.deko.mall.system.service.storage.SysFileStorageFactory;
import com.deko.mall.system.service.storage.SysFileStorageService;
import com.deko.mall.system.vo.FileInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "06.文件接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class SysFileController {

    /**
     * 微信小程序码缓存过期时间：24小时（单位：秒）
     */
    private static final long WXACODE_CACHE_EXPIRE_SECONDS = 24 * 60 * 60;
    /**
     * 微信小程序码缓存前缀
     */
    private static final String WXACODE_CACHE_PREFIX = "wxacode:cache:";
    private final SysFileStorageFactory sysFileStorageFactory;
    private final StringRedisTemplate redisTemplate;
    @Value("${wx.miniapp.appId}")
    private String appId;

    @Value("${wx.miniapp.appSecret}")
    private String appSecret;

    @Value("${app.h5.domain:}")
    private String h5Domain;


    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<FileInfoVO> uploadFile(
            @Parameter(name = "表单文件对象") @RequestParam(value = "file") MultipartFile file
    ) {
        SysFileStorageService storageService = sysFileStorageFactory.getDefaultFileStorageService();
        FileInfoVO fileInfo = storageService.uploadFile(file);

        return Result.success(fileInfo);
    }

    @PostMapping("/delete")
    @Operation(summary = "文件删除")
    public Result deleteFile(
            @Parameter(name = "文件路径") @RequestParam String filePath
    ) {
        SysFileStorageService storageService = sysFileStorageFactory.getDefaultFileStorageService();
        boolean result = storageService.deleteFile(filePath);
        return Result.judge(result);
    }

    /**
     * 二维码生成接口 根据字段或链接内容生成二维码 并上传OSS 存储，同时返回二维码地址
     */
    @Operation(summary = "二维码生成接口")
    @GetMapping("/qrcode")
    public Result<FileInfoVO> generateQrCode(
            @Parameter(name = "二维码内容") @RequestParam String content
    ) {
        Map<EncodeHintType, Object> encodeMap = Maps.newHashMap();
        /* 设置纠错级别(L 7%~M 15%~Q 25%~H 30%),纠错级别越高存储的信息越少 */
        encodeMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        /* 设置编码格式 */
        encodeMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        /* 设置边缘空白 */
        encodeMap.put(EncodeHintType.MARGIN, 1);

        // 定义一个输出流，生成二维码保存在输出流里
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            MatrixToImageWriter.writeToStream(new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE, 300, 300, encodeMap), "png", os);
        } catch (Exception e) {
            e.printStackTrace();
            Result.failed("二维码生成失败");
        }
        byte[] bytes = os.toByteArray();
        String fileName = "qrcode-" + System.currentTimeMillis() + ".png";

        SysFileStorageService storageService = sysFileStorageFactory.getDefaultFileStorageService();

        FileInfoVO fileInfo = storageService.uploadFile(new MultipartFileBO("file", fileName, null, bytes));
        return Result.success(fileInfo);
    }

    /**
     * 邀请海报生成接口
     * 后端合成海报图片（背景 + 二维码 + 文字），上传 OSS，返回图片 URL
     */
    @Operation(summary = "生成邀请海报")
    @GetMapping("/poster")
    public Result<Map<String, String>> generatePoster(
            @Parameter(name = "分享链接（用于生成二维码）") @RequestParam String url
    ) {
        try {
            // 1. 生成 QR 码图片
            BufferedImage qrImage = createQrImage(url);

            // 2. 上传 QR 码到 OSS
            byte[] qrBytes = imageToBytes(qrImage, "png");
            SysFileStorageService storageService = sysFileStorageFactory.getDefaultFileStorageService();
            FileInfoVO qrFileInfo = storageService.uploadFile(
                    new MultipartFileBO("file", "qrcode-" + System.currentTimeMillis() + ".png", null, qrBytes));

            // 3. 合成海报
            BufferedImage poster = composePoster(qrImage);

            // 4. 上传海报到 OSS
            byte[] posterBytes = imageToBytes(poster, "png");
            FileInfoVO posterFileInfo = storageService.uploadFile(
                    new MultipartFileBO("file", "poster-" + System.currentTimeMillis() + ".png", null, posterBytes));

            // 5. 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("user_share_poster", posterFileInfo.getUrl());
            result.put("user_share_qrode", qrFileInfo.getUrl());
            result.put("user_share_url", url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成海报失败", e);
            return Result.failed("海报生成失败：" + e.getMessage());
        }
    }

    // 海报合成辅助方法

    /** 创建 QR 码 BufferedImage */
    private BufferedImage createQrImage(String content) throws Exception {
        Map<EncodeHintType, Object> encodeMap = Maps.newHashMap();
        encodeMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        encodeMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        encodeMap.put(EncodeHintType.MARGIN, 1);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(
                new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300, encodeMap),
                "png", os);
        return ImageIO.read(new java.io.ByteArrayInputStream(os.toByteArray()));
    }

    /** 可用的中文字体列表（按优先级尝试） */
    private static final String[] CJK_FONT_NAMES = {
        "WenQuanYi Micro Hei", "Noto Sans CJK SC", "Noto Sans SC",
        "SimHei", "Microsoft YaHei", "PingFang SC", "PingFang TC",
        "Heiti SC", "Hiragino Sans GB", "SansSerif"
    };
    private static Font cachedCjkFont;

    /** 获取可用的中文字体（首次调用时自动检测，后续缓存） */
    private Font getCjkFont(int style, int size) {
        Font base = cachedCjkFont;
        if (base != null) {
            return base.deriveFont(style, (float) size);
        }
        for (String name : CJK_FONT_NAMES) {
            Font f = new Font(name, Font.PLAIN, 12);
            if (f.canDisplayUpTo("示例商城邀请扫码加入") == -1) {
                cachedCjkFont = f;
                log.info("海报中文字体: {}", name);
                return f.deriveFont(style, (float) size);
            }
        }
        log.warn("未找到可用的中文字体，海报中文将显示为方框");
        return new Font("SansSerif", style, size);
    }

    /** 合成邀请海报 */
    private BufferedImage composePoster(BufferedImage qrImage) {
        int W = 400, H = 460;
        BufferedImage poster = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = poster.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 渐变背景（浅暖色 → 白）
        GradientPaint gradient = new GradientPaint(0, 0, new Color(0xff, 0xf0, 0xed), 0, H, Color.WHITE);
        g.setPaint(gradient);
        g.fillRect(0, 0, W, H);

        // 标题
        g.setFont(getCjkFont( Font.BOLD, 26));
        g.setColor(new Color(0xe0, 0x85, 0x7a));
        drawCenteredString(g, "示例商城", W / 2, 80);

        // 副标题
        g.setFont(getCjkFont( Font.PLAIN, 18));
        g.setColor(new Color(0x99, 0x99, 0x99));
        drawCenteredString(g, "邀请你一起来逛", W / 2, 115);

        // QR 码白色卡片
        int qrSize = 190;
        int qrX = (W - qrSize) / 2;
        int qrY = 155;
        int pad = 16;

        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(qrX - pad, qrY - pad, qrSize + pad * 2, qrSize + pad * 2, 12, 12));

        // QR 码
        g.drawImage(qrImage, qrX, qrY, qrSize, qrSize, null);

        // 引导文字
        g.setFont(getCjkFont( Font.PLAIN, 13));
        g.setColor(new Color(0x99, 0x99, 0x99));
        drawCenteredString(g, "扫码上方二维码即可加入", W / 2, qrY + qrSize + pad + 22);

        g.dispose();
        return poster;
    }

    /** 居中绘制文字 */
    private void drawCenteredString(Graphics2D g, String text, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = cx - fm.stringWidth(text) / 2;
        g.drawString(text, x, y);
    }

    /** BufferedImage 转 byte[] */
    private byte[] imageToBytes(BufferedImage image, String format) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, format, os);
        return os.toByteArray();
    }

    /**
     * 生成微信小程序码 并上传OSS存储，返回微信码地址
     * <p>
     * 与普通二维码不同，微信码：
     * 1. 更美观，可定制颜色和样式
     * 2. 永久有效，无数量限制
     * 3. 体积更小，识别速度更快
     * <p>
     * 使用Redis缓存相同的请求结果，防止频繁请求微信接口
     */
    @Operation(summary = "生成微信小程序码")
    @GetMapping("/wxacode")
    public Result<FileInfoVO> generateWxaCode(
            @Parameter(description = "小程序页面路径，如 pages/index/index") @RequestParam String path,
            @Parameter(description = "参数，最大32个可见字符") @RequestParam(required = false) String scene,
            @Parameter(description = "二维码宽度，单位px") @RequestParam(required = false, defaultValue = "430") Integer width,
            @Parameter(description = "是否透明底色") @RequestParam(required = false, defaultValue = "false") Boolean isHyaline,
            @Parameter(description = "主色调，形如 #FF0000") @RequestParam(required = false) String lineColor
    ) {
        try {
            log.info("生成微信小程序码, path={}, scene={}, width={}", path, scene, width);

            // URL解码scene参数（处理客户端双重编码的情况），确保传递给微信API的scene值不超过32字符
            if (scene != null && !scene.isEmpty()) {
                scene = URLDecoder.decode(scene, StandardCharsets.UTF_8);
                log.info("URL解码后的scene: {}", scene);
            }

            // 构建缓存key（基于请求参数）
            String cacheKey = buildCacheKey(path, scene, width, isHyaline, lineColor);

            // 1. 先从Redis缓存中获取
            String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
            if (cachedUrl != null && !cachedUrl.isEmpty()) {
                log.info("从Redis缓存获取微信小程序码, cacheKey={}", cacheKey);
                FileInfoVO cachedInfo = new FileInfoVO();
                cachedInfo.setUrl(cachedUrl);
                return Result.success(cachedInfo);
            }

            // 2. 获取微信接口调用凭证(access_token)
            String accessToken = getWxAccessToken();
            if (accessToken == null) {
                return Result.failed("获取微信接口调用凭证失败");
            }

            // 3. 调用微信接口生成小程序码
            String wxaCodeApiUrl;
            Map<String, Object> params = new HashMap<>();

            if (scene == null || scene.isEmpty()) {
                // 适用于pages/index/index这样不带参数的固定页面，永久有效，有数量限制
                wxaCodeApiUrl = "https://api.weixin.qq.com/wxa/getwxacode?access_token=" + accessToken;
                params.put("path", path); // 扫码进入的小程序页面路径
            } else {
                // 适合需要场景值的情况，无限制
                wxaCodeApiUrl = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;
                params.put("scene", scene); // 场景值，最大32个可见字符
                params.put("page", path);  // 小程序页面路径
            }

            // 公共参数
            params.put("width", width); // 码的宽度
            params.put("is_hyaline", isHyaline); // 是否需要透明底色

            // 如果指定了颜色
            if (lineColor != null && !lineColor.isEmpty()) {
                // 解析颜色
                if (lineColor.startsWith("#") && lineColor.length() == 7) {
                    Map<String, Object> colorMap = new HashMap<>();
                    // 将#FF0000转为r,g,b值
                    String hex = lineColor.substring(1);
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);

                    colorMap.put("r", r);
                    colorMap.put("g", g);
                    colorMap.put("b", b);
                    params.put("line_color", colorMap);
                }
            }

            // 4. 发送请求获取小程序码二进制数据
            log.info("调用微信小程序码API: {}, 参数: {}", wxaCodeApiUrl, params);
            byte[] wxaCodeBytes = HttpUtil.createPost(wxaCodeApiUrl)
                    .body(JSONUtil.toJsonStr(params))
                    .execute()
                    .bodyBytes();

            // 检查返回内容是否为JSON（错误消息），而不是二进制图片
            if (wxaCodeBytes.length > 0 && wxaCodeBytes[0] == '{') {
                String errorMsg = new String(wxaCodeBytes);
                log.error("生成微信小程序码失败: {}", errorMsg);
                return Result.failed("生成微信小程序码失败: " + errorMsg);
            }

            // 5. 上传到OSS存储
            String fileName = "wxacode-" + System.currentTimeMillis() + ".png";
            SysFileStorageService storageService = sysFileStorageFactory.getDefaultFileStorageService();
            FileInfoVO fileInfo = storageService.uploadFile(new MultipartFileBO("file", fileName, null, wxaCodeBytes));

            // 6. 存入Redis缓存
            redisTemplate.opsForValue().set(cacheKey, fileInfo.getUrl(), WXACODE_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.info("微信小程序码已存入Redis缓存, cacheKey={}, url={}", cacheKey, fileInfo.getUrl());

            return Result.success(fileInfo);
        } catch (Exception e) {
            log.error("生成微信小程序码失败", e);
            return Result.failed("微信小程序码生成失败：" + e.getMessage());
        }
    }

    /**
     * 构建微信小程序码缓存key
     */
    private String buildCacheKey(String path, String scene, Integer width, Boolean isHyaline, String lineColor) {
        StringBuilder keyBuilder = new StringBuilder(WXACODE_CACHE_PREFIX);
        keyBuilder.append(path);
        if (scene != null && !scene.isEmpty()) {
            keyBuilder.append(":").append(scene);
        }
        keyBuilder.append(":").append(width);
        keyBuilder.append(":").append(isHyaline);
        if (lineColor != null && !lineColor.isEmpty()) {
            keyBuilder.append(":").append(lineColor);
        }
        return keyBuilder.toString();
    }

    /**
     * 获取微信接口调用凭证
     *
     * @return access_token 或 null(获取失败)
     */
    private String getWxAccessToken() {
        try {
            // 校验必要参数
            if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
                log.error("微信小程序配置缺失，请检查配置文件中的wx.miniapp.app-id和wx.miniapp.secret配置");
                return null;
            }

            // 接口地址
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;

            // 发送请求
            String response = HttpUtil.get(url);
            log.debug("获取微信access_token响应: {}", response);

            // 解析响应
            JSONObject json = JSONUtil.parseObj(response);

            // 检查是否有错误
            if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
                log.error("获取微信access_token失败: {}", json);
                return null;
            }

            // 返回access_token
            return json.getStr("access_token");
        } catch (Exception e) {
            log.error("获取微信access_token异常", e);
            return null;
        }
    }


}
