package com.deko.mall.cms.service;

import com.deko.mall.cms.model.vo.DiyApiInitVO;

/**
 * DIY 装修器 API 服务接口
 * 提供装修器初始化、数据获取等功能
 */
public interface DiyApiService {

    /**
     * 装修器初始化
     * 获取分类、品牌、商品分类等基础数据
     *
     * @return 初始化数据
     */
    DiyApiInitVO init();

    /**
     * 获取 App 底部导航（tabbar）装修配置
     * app_tabbar_data：按业务 key（home）读取 cms_diy_app_tabbar
     *
     * @param business 业务标识（缺省/空/home）
     * @return 底部导航配置对象（content/style），未配置返回 null
     */
    Object getAppTabbarData(String business);

    /**
     * 保存 App 底部导航（tabbar）装修配置
     * app_tabbar_save：原样 JSON 序列化写入 cms_diy_app_tabbar
     *
     * @param business 业务标识（缺省/空/home）
     * @param config   底部导航装修配置（content/style）
     */
    void saveAppTabbar(String business, Object config);
}

