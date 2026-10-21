package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.model.entity.DiyHomeMode;

import java.util.Map;

/**
 * DIY 首页按端配置服务
 * 每端(client_type)一行指定首页 DIY 页面 + 定时
 */
public interface DiyHomeModeService extends IService<DiyHomeMode> {

    /**
     * 读取全量按端配置（供 admin 弹窗回显）
     *
     * @return {weixin:{diyId,timeStart,timeEnd}, app:{...}, h5:{...}}，未配置端不出现
     */
    Map<String, Object> getHomeModeConfigMap();

    /**
     * 覆盖保存全量按端配置（前端提交的三端行合并入库）
     *
     * @param configMap 前端契约 {weixin:{diy_id,time_start,time_end}, app:{...}, h5:{...}}
     */
    void saveHomeModeConfigMap(Map<String, Object> configMap);

    /**
     * 按端读取首页 DIY 页面（含定时窗口校验，不做页面启用校验——由调用方校验）
     *
     * @param clientType 端类型 weixin/app/h5
     * @return 配置行；未配置/定时外返回 null
     */
    DiyHomeMode getByClientTypeInWindow(String clientType);
}
