package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.model.entity.DiyAppTabbar;

/**
 * DIY 底部导航装修数据服务
 * 按 business(home) 存取底部导航装修配置
 */
public interface DiyAppTabbarService extends IService<DiyAppTabbar> {

    /**
     * 读取底部导航装修配置（business=home），未配置返回 null
     *
     * @param business 业务标识（缺省/空/home）
     * @return 配置对象（content/style），未配置返回 null
     */
    Object getTabbarConfig(String business);

    /**
     * 保存底部导航装修配置（business=home，原样 JSON 序列化）
     *
     * @param business 业务标识（缺省/空/home）
     * @param config   装修配置（content/style）
     */
    void saveTabbarConfig(String business, Object config);
}
