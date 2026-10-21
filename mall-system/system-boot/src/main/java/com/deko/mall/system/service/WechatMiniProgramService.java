package com.deko.mall.system.service;

import java.util.Map;

/**
 * 微信小程序服务接口
 */
public interface WechatMiniProgramService {

    /**
     * 生成微信小程序 URL Link
     *
     * @param path 小程序页面路径
     * @param scene 场景参数（最长 32 个字符）
     * @param envVersion 小程序版本（release/trial/develop）
     * @param isShorten 是否生成短链
     * @return 包含 url_link 的 Map
     * @throws Exception 生成失败时抛出异常
     */
    Map<String, String> generateUrlLink(String path, String scene, String envVersion, boolean isShorten) throws Exception;

    /**
     * 获取小程序二维码
     *
     * @param scene 场景值 ID（最长 32 个字符）
     * @param page 小程序页面路径
     * @param checkPath 是否检查 page 是否存在
     * @param envVersion 小程序版本
     * @return 包含二维码 base64 的 Map
     * @throws Exception 获取失败时抛出异常
     */
    Map<String, String> getMiniProgramCode(String scene, String page, boolean checkPath, String envVersion) throws Exception;

    /**
     * 获取分享配置（用于APP和H5分享小程序卡片）
     *
     * @param spuId 商品ID
     * @param shareId 分享人ID（可选）
     * @param page 小程序页面路径
     * @return 分享配置信息
     * @throws Exception 获取失败时抛出异常
     */
    Map<String, Object> getShareConfig(Long spuId, String shareId, String page) throws Exception;
}
