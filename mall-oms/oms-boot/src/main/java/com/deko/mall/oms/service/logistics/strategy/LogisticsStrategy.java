package com.deko.mall.oms.service.logistics.strategy;

import com.deko.mall.oms.enums.LogisticsProviderEnum;
import com.deko.mall.oms.model.dto.LogisticsDTO;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;

import java.util.List;

/**
 * 物流策略接口
 */
public interface LogisticsStrategy {

    /**
     * 获取物流供应商
     */
    LogisticsProviderEnum getProvider();

    /**
     * 订阅物流轨迹
     *
     * @param subscribeDTO 物流对象
     */
    void subscribeTrack(LogisticsDTO subscribeDTO);

    /**
     * 查询物流轨迹
     *
     * @param trackingNo 物流单号
     * @return 物流轨迹列表
     */
    List<OmsLogisticsTrack> queryTracks(String trackingNo, String carrierCode, String phone);

    /**
     * 处理物流回调
     *
     * @param param 物流轨迹参数(JSON字符串)
     * @param sign  回调签名
     * @return 物流轨迹列表
     */
    List<OmsLogisticsTrack> handleCallback(String param, String sign);
}
