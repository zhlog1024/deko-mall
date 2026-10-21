package com.deko.mall.oms.service.logistics;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.oms.model.dto.LogisticsDTO;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsInfo;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;
import com.deko.mall.oms.model.vo.CarrierVO;
import com.deko.mall.oms.model.vo.LogisticsDetailVO;

import java.util.List;
import com.deko.mall.oms.model.dto.ShippingInfoUploadDTO;

/**
 * 物流服务接口
 */
public interface LogisticsInfoService extends IService<OmsLogisticsInfo> {

    /**
     * 订阅发货物流轨迹
     *
     * @param subscribeDTO 物流信息DTO
     * @return 订阅结果
     */
    boolean subscribeLogistics(LogisticsDTO subscribeDTO);

    /**
     * 订阅退货物流轨迹
     *
     * @param subscribeDTO 物流信息DTO
     * @return 订阅结果
     */
    boolean subscribeReturnLogistics(LogisticsDTO subscribeDTO);

    /**
     * 查询实时物流轨迹
     */
    boolean compensateQueryTrack(String trackingNo);

    /**
     * 获取物流详情
     */
    LogisticsDetailVO queryLogisticsDetail(String trackingNo);

    List<OmsLogisticsTrack> queryBizTracks(String orderNo);

    void updateTrack(OmsLogisticsTrack track);

    OmsLogisticsInfo getByTrackingNo(String trackingNo);

    /**
     * 获取快递公司列表
     */
    List<CarrierVO> getCarrierList();

    /**
     * 查询待补偿的物流订单
     *
     * @return 未签收且需要补偿查询的物流订单列表
     */
    List<OmsLogisticsInfo> getCompensateOrders();

    /**
     * 处理快递100回调
     *
     * @param param 回调的物流轨迹参数(JSON字符串)
     * @param sign  回调签名
     * @return 是否处理成功
     */
    boolean handleCallback(String param, String sign);

    String uploadShippingInfo(ShippingInfoUploadDTO dto);
}
