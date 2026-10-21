package com.deko.mall.oms.service.logistics.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.oms.config.OmsResultCode;
import com.deko.mall.oms.constant.LogisticsCacheConstants;
import com.deko.mall.oms.enums.LogisticsCarrierEnum;
import com.deko.mall.oms.enums.LogisticsProviderEnum;
import com.deko.mall.oms.enums.LogisticsStatusEnum;
import com.deko.mall.oms.enums.LogisticsTypeEnum;
import com.deko.mall.oms.mapper.LogisticsInfoMapper;
import com.deko.mall.oms.mapper.LogisticsTrackMapper;
import com.deko.mall.oms.model.dto.LogisticsDTO;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsInfo;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;
import com.deko.mall.oms.model.event.LogisticsSignedEvent;
import com.deko.mall.oms.model.vo.CarrierVO;
import com.deko.mall.oms.model.vo.LogisticsDetailVO;
import com.deko.mall.oms.service.logistics.LogisticsInfoService;
import com.deko.mall.oms.service.logistics.strategy.LogisticsStrategy;
import com.deko.mall.oms.service.logistics.strategy.LogisticsStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.deko.mall.oms.model.dto.ShippingInfoUploadDTO;

/**
 * 物流服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsInfoServiceImpl extends ServiceImpl<LogisticsInfoMapper, OmsLogisticsInfo> implements LogisticsInfoService {

    private static final DateTimeFormatter SHIPPING_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final String WX_SHIPPING_API_URL = "https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info";
    private static final String SUCCESS_CODE = "0";
    private static final String ERROR_CODE = "-1";
    private final LogisticsStrategyFactory logisticsStrategyFactory;
    private final LogisticsInfoMapper logisticsInfoMapper;
    private final LogisticsTrackMapper logisticsTrackMapper;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${wx.miniapp.appId}")
    private String appId;
    @Value("${wx.miniapp.appSecret}")
    private String appSecret;
    @Value("${wx.miniapp.mchId}")
    private String mchId;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean subscribeLogistics(LogisticsDTO logisticsDTO) {
        try {
            // 1. 创建物流运单
            OmsLogisticsInfo logisticsOrder = new OmsLogisticsInfo();
            logisticsOrder.setBizNo(logisticsDTO.getBizNo());
            logisticsOrder.setTrackingNo(logisticsDTO.getTrackingNo());
            logisticsOrder.setCarrierCode(logisticsDTO.getCarrierCode());
            logisticsOrder.setPhone(logisticsDTO.getPhone());
            logisticsOrder.setCarrierName(IBaseEnum.getLabelByValue(logisticsDTO.getCarrierCode(), LogisticsCarrierEnum.class));
            logisticsOrder.setStatus(LogisticsStatusEnum.PENDING.getValue());

            // 设置物流类型
            if (logisticsDTO.getLogisticsType() != null) {
                logisticsOrder.setLogisticsType(logisticsDTO.getLogisticsType());
                logisticsOrder.setRemark("LogisticsType:" + logisticsDTO.getLogisticsType());
            }

            logisticsInfoMapper.insert(logisticsOrder);

            // 2. 快递100订阅物流轨迹
            LogisticsStrategy kuaidi100Strategy = logisticsStrategyFactory.getStrategy(LogisticsProviderEnum.KUAIDI100.getValue());
            kuaidi100Strategy.subscribeTrack(logisticsDTO);

            // 3.微信小程序物流订阅
            LogisticsStrategy wxMiniStrategy = logisticsStrategyFactory.getStrategy(LogisticsProviderEnum.WX_MINIAPP.getValue());
            wxMiniStrategy.subscribeTrack(logisticsDTO);

            return true;
        } catch (Exception e) {
            log.error("订阅物流失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean subscribeReturnLogistics(LogisticsDTO logisticsDTO) {
        try {
            // 确保物流类型是退货物流
            logisticsDTO.setLogisticsType(LogisticsTypeEnum.RETURN.getValue());

            // 1. 创建退货物流运单
            OmsLogisticsInfo logisticsOrder = new OmsLogisticsInfo();
            logisticsOrder.setBizNo(logisticsDTO.getBizNo()); // 售后单编号
            logisticsOrder.setLogisticsType(LogisticsTypeEnum.RETURN.getValue());
            logisticsOrder.setTrackingNo(logisticsDTO.getTrackingNo());
            logisticsOrder.setCarrierCode(logisticsDTO.getCarrierCode());
            logisticsOrder.setPhone(logisticsDTO.getPhone());
            logisticsOrder.setCarrierName(IBaseEnum.getLabelByValue(logisticsDTO.getCarrierCode(), LogisticsCarrierEnum.class));
            logisticsOrder.setStatus(LogisticsStatusEnum.PENDING.getValue());
            logisticsOrder.setRemark("LogisticsType:" + LogisticsTypeEnum.RETURN.getValue());
            logisticsInfoMapper.insert(logisticsOrder);

            // 2. 快递100订阅退货物流轨迹
            LogisticsStrategy kuaidi100Strategy = logisticsStrategyFactory.getStrategy(LogisticsProviderEnum.KUAIDI100.getValue());
            kuaidi100Strategy.subscribeTrack(logisticsDTO);

            log.info("售后退货物流订阅成功，售后单号：{}，物流单号：{}", logisticsDTO.getBizNo(), logisticsDTO.getTrackingNo());
            return true;
        } catch (Exception e) {
            log.error("订阅退货物流失败", e);
            return false;
        }
    }

    @Override
    public OmsLogisticsInfo getByTrackingNo(String trackingNo) {
        return logisticsInfoMapper.selectOne(
                new LambdaQueryWrapper<OmsLogisticsInfo>()
                        .eq(OmsLogisticsInfo::getTrackingNo, trackingNo)
                        .orderByDesc(OmsLogisticsInfo::getCreateTime)
        );
    }

    @Override
    public List<OmsLogisticsTrack> queryBizTracks(String orderNo) {
        OmsLogisticsInfo logisticsOrder = this.getByTrackingNo(orderNo);
        if (logisticsOrder == null) {
            return List.of();
        }

        // 先从缓存获取
        String cacheKey = LogisticsCacheConstants.LOGISTICS_TRACK_PREFIX + logisticsOrder.getTrackingNo();
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            return JSONUtil.toList(cacheValue, OmsLogisticsTrack.class);
        }

        // 缓存未命中，从数据库查询
        List<OmsLogisticsTrack> tracks = logisticsTrackMapper.selectList(
                new LambdaQueryWrapper<OmsLogisticsTrack>()
                        .eq(OmsLogisticsTrack::getLogisticsId, logisticsOrder.getId())
                        .orderByAsc(OmsLogisticsTrack::getEventTime)
        );

        // 缓存查询结果
        if (!tracks.isEmpty()) {
            cacheLogisticsTracks(logisticsOrder.getTrackingNo(), tracks);
        }

        return tracks;
    }

    @Override
    public void updateTrack(OmsLogisticsTrack track) {
        if (track == null) {
            return;
        }

        // 1. 检查轨迹是否存在
        boolean exists = logisticsTrackMapper.exists(
                new LambdaQueryWrapper<OmsLogisticsTrack>()
                        .eq(OmsLogisticsTrack::getLogisticsId, track.getLogisticsId())
                        .eq(OmsLogisticsTrack::getEventTime, track.getEventTime())
                        .eq(OmsLogisticsTrack::getTrackEvent, track.getTrackEvent())
        );

        if (!exists) {
            // 2. 获取物流订单ID
            OmsLogisticsInfo logistics = this.getByTrackingNo(track.getTrackingNo());
            if (logistics != null) {
                track.setLogisticsId(logistics.getId());
                // 3. 保存轨迹
                logisticsTrackMapper.insert(track);

                // 4. 更新物流状态
                logistics.setStatus(track.getStatus());
                logisticsInfoMapper.updateById(logistics);
            }
        }
    }

    @Override
    public List<CarrierVO> getCarrierList() {
        return Arrays.stream(LogisticsCarrierEnum.values())
                .map(carrier -> CarrierVO.builder()
                        .carrierCode(carrier.getValue())
                        .carrierName(carrier.getLabel())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean compensateQueryTrack(String trackingNo) {
        // 1. 获取物流运单
        OmsLogisticsInfo logistics = this.getByTrackingNo(trackingNo);
        if (logistics == null) {
            return false;
        }

        try {
            // 2. 查询物流轨迹
            LogisticsStrategy strategy = logisticsStrategyFactory.getStrategy(LogisticsProviderEnum.KUAIDI100.getValue());
            List<OmsLogisticsTrack> tracks = strategy.queryTracks(trackingNo, logistics.getCarrierCode(), logistics.getPhone());

            // 3. 更新物流状态
            if (tracks != null && !tracks.isEmpty()) {
                // 3.1 更新最新物流状态
                OmsLogisticsTrack latestTrack = tracks.get(0);
                logistics.setStatus(latestTrack.getStatus());
                logistics.setLastQueryTime(LocalDateTime.now());
                logistics.setQueryCount(logistics.getQueryCount() + 1);
                logisticsInfoMapper.updateById(logistics);

                // 3.2 保存新增的轨迹记录
                for (OmsLogisticsTrack track : tracks) {
                    boolean exists = logisticsTrackMapper.exists(
                            new LambdaQueryWrapper<OmsLogisticsTrack>()
                                    .eq(OmsLogisticsTrack::getTrackingNo, track.getTrackingNo())
                                    .eq(OmsLogisticsTrack::getEventTime, track.getEventTime())
                                    .eq(OmsLogisticsTrack::getTrackEvent, track.getTrackEvent())
                    );
                    if (!exists) {
                        track.setLogisticsId(logistics.getId());
                        logisticsTrackMapper.insert(track);
                    }
                }

                // 3.3 缓存轨迹列表
                cacheLogisticsTracks(trackingNo, tracks);

                // 3.4 签收时发布事件（仅发货物流）
                publishSignedEventIfNeeded(logistics, latestTrack);
            }

            return true;
        } catch (Exception e) {
            log.error("补偿查询物流轨迹异常，trackingNo={}", trackingNo, e);
            throw new BizException(OmsResultCode.LOGISTICS_COMPENSATE_QUERY_FAILED);
        }
    }

    @Override
    public LogisticsDetailVO queryLogisticsDetail(String trackingNo) {
        // 1. 查询物流运单信息
        OmsLogisticsInfo logisticsInfo = this.getByTrackingNo(trackingNo);

        if (logisticsInfo == null) {
            throw new BizException(OmsResultCode.LOGISTICS_WAYBILL_NOT_EXIST);
        }

        // 2. 查询物流轨迹（按时间正序）
        List<OmsLogisticsTrack> tracks = this.queryBizTracks(trackingNo);

        // 3. 组装物流详情
        LogisticsDetailVO detail = new LogisticsDetailVO();
        detail.setBizNo(trackingNo);
        detail.setLogisticsType(logisticsInfo.getLogisticsType());
        detail.setLogisticsTypeLabel(IBaseEnum.getLabelByValue(logisticsInfo.getLogisticsType(), LogisticsTypeEnum.class));
        detail.setTrackingNo(logisticsInfo.getTrackingNo());
        detail.setCarrierCode(logisticsInfo.getCarrierCode());
        detail.setCarrierName(IBaseEnum.getLabelByValue(logisticsInfo.getCarrierCode(), LogisticsCarrierEnum.class));
        detail.setStatus(logisticsInfo.getStatus());
        detail.setStatusDesc(IBaseEnum.getLabelByValue(logisticsInfo.getStatus(), LogisticsStatusEnum.class));
        detail.setTracks(tracks);

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleCallback(String param, String sign) {
        if (StringUtils.isBlank(param)) {
            throw new BizException(OmsResultCode.CALLBACK_PARAMS_CANNOT_BE_EMPTY);
        }

        try {
            log.info("处理快递100回调请求");
            // 1. 获取物流策略
            LogisticsStrategy strategy = logisticsStrategyFactory.getStrategy(LogisticsProviderEnum.KUAIDI100.getValue());
            if (strategy == null) {
                throw new BizException(OmsResultCode.LOGISTICS_STRATEGY_NOT_FOUND);
            }

            // 2. 解析物流轨迹
            List<OmsLogisticsTrack> tracks = strategy.handleCallback(param, sign);
            if (tracks == null || tracks.isEmpty()) {
                log.warn("物流轨迹解析失败");
                return false;
            }

            // 3. 获取最新轨迹(按时间倒序)
            tracks.sort((a, b) -> b.getEventTime().compareTo(a.getEventTime()));
            OmsLogisticsTrack latestTrack = tracks.get(0);

            // 4. 更新物流运单状态
            OmsLogisticsInfo logisticsOrder = this.getByTrackingNo(latestTrack.getTrackingNo());
            if (logisticsOrder == null) {
                log.warn("物流运单不存在, trackingNo={}", latestTrack.getTrackingNo());
                return false;
            }
            logisticsOrder.setStatus(latestTrack.getStatus());
            logisticsOrder.setLastCallbackTime(LocalDateTime.now());
            this.updateById(logisticsOrder);

            // 5. 保存轨迹信息
            for (OmsLogisticsTrack track : tracks) {
                track.setLogisticsId(logisticsOrder.getId());
                this.updateTrack(track);
            }

            // 6. 缓存轨迹信息
            this.cacheLogisticsTracks(latestTrack.getTrackingNo(), tracks);

            // 7. 签收时发布事件（仅发货物流）
            publishSignedEventIfNeeded(logisticsOrder, latestTrack);

            log.info("处理快递100回调成功: trackingNo={}, status={}, trackCount={}",
                    latestTrack.getTrackingNo(), latestTrack.getStatus(), tracks.size());
            return true;
        } catch (Exception e) {
            log.error("处理快递100回调异常", e);
            throw new BizException(OmsResultCode.LOGISTICS_KD100_CALLBACK_FAILED);
        }
    }

    @Override
    public List<OmsLogisticsInfo> getCompensateOrders() {
        LocalDateTime now = LocalDateTime.now();
        return logisticsInfoMapper.selectList(new LambdaQueryWrapper<OmsLogisticsInfo>()
                .ne(OmsLogisticsInfo::getStatus, LogisticsStatusEnum.SIGNED.getValue())
                .ge(OmsLogisticsInfo::getCreateTime, now.minusDays(30))
                .and(w -> w.isNull(OmsLogisticsInfo::getLastQueryTime)
                        .or().le(OmsLogisticsInfo::getLastQueryTime, now.minusHours(2)))
                .last("LIMIT 100"));
    }

    /**
     * 缓存物流轨迹列表
     */
    private void cacheLogisticsTracks(String trackingNo, List<OmsLogisticsTrack> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            return;
        }
        // 按时间倒序
        tracks.sort((a, b) -> b.getEventTime().compareTo(a.getEventTime()));

        String cacheKey = LogisticsCacheConstants.LOGISTICS_TRACK_PREFIX + trackingNo;
        String value = JSONUtil.toJsonStr(tracks);
        redisTemplate.opsForValue().set(cacheKey, value, LogisticsCacheConstants.LOGISTICS_TRACK_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 物流已签收时发布事件（仅发货物流类型触发，由监听器处理订单自动确认收货）
     */
    private void publishSignedEventIfNeeded(OmsLogisticsInfo logistics, OmsLogisticsTrack latestTrack) {
        if (!LogisticsStatusEnum.SIGNED.getValue().equals(latestTrack.getStatus())) {
            return;
        }
        if (!LogisticsTypeEnum.DELIVERY.getValue().equals(logistics.getLogisticsType())) {
            return;
        }
        try {
            eventPublisher.publishEvent(new LogisticsSignedEvent(
                    this, logistics.getBizNo(), logistics.getTrackingNo(), logistics.getLogisticsType()));
            log.info("发布物流签收事件: bizNo={}, trackingNo={}", logistics.getBizNo(), logistics.getTrackingNo());
        } catch (Exception e) {
            log.error("发布物流签收事件失败: bizNo={}", logistics.getBizNo(), e);
        }
    }

    /**
     * 构建发货请求体
     */
    private JSONObject buildShippingRequestBody(String mchId, String outTradeNo, String openid, String desc,
                                                String expressCompany, String trackingNo) {
        // 订单信息
        JSONObject orderKey = new JSONObject();
        orderKey.put("order_number_type", 1);  // 使用微信支付订单号
        orderKey.put("mchid", mchId);
        orderKey.put("out_trade_no", outTradeNo);  // 使用微信支付交易号

        // 物流信息
        JSONObject shippingInfo = new JSONObject();
        shippingInfo.put("item_desc", StrUtil.isBlank(desc) ? "商品发货" : desc);
        shippingInfo.put("tracking_no", trackingNo);
        shippingInfo.put("express_company", expressCompany);
        shippingInfo.put("delivery_mode", 1);  // 快递发货

        // 支付者信息
        JSONObject payer = new JSONObject();
        payer.put("openid", openid);

        // 发货清单
        JSONArray shippingList = new JSONArray();
        shippingList.add(shippingInfo);

        // 组装请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("order_key", orderKey);
        requestBody.put("delivery_mode", 1);  // 统一发货
        requestBody.put("logistics_type", 1);  // 实体物流配送
        requestBody.put("shipping_list", shippingList);
        requestBody.put("upload_time", ZonedDateTime.now(ZoneId.systemDefault()).format(SHIPPING_TIME_FORMATTER));
        requestBody.put("payer", payer);

        return requestBody;
    }

    /**
     * 上传小程序发货信息
     *
     * @param outTradeNo     微信商户订单号
     * @param openid         买家openid
     * @param desc           商品描述
     * @param expressCompany 快递公司编码
     * @param trackingNo     快递单号
     * @return 错误码，0表示成功
     */
    @Override
    public String uploadShippingInfo(ShippingInfoUploadDTO dto) {
        String outTradeNo = dto.getOutTradeNo();
        String openid = dto.getOpenid();
        String desc = dto.getDesc();
        String expressCompany = dto.getExpressCompany();
        String trackingNo = dto.getTrackingNo();
        try {
            String accessToken = getAccessToken(appId, appSecret);

            if (StrUtil.hasBlank(outTradeNo, openid, accessToken)) {
                log.error("参数错误：微信支付交易号、openid、accessToken 不能为空");
                return ERROR_CODE;
            }

            String url = WX_SHIPPING_API_URL + "?access_token=" + accessToken;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            JSONObject requestBody = buildShippingRequestBody(mchId, outTradeNo, openid, desc, expressCompany, trackingNo);
            String jsonRequest = requestBody.toString();

            log.info("微信小程序发货接口调用，URL：{}，参数：{}", url, jsonRequest);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            String responseBody = response.getBody();
            log.info("微信小程序发货接口响应：{}", responseBody);

            if (!response.getStatusCode().is2xxSuccessful() || StrUtil.isBlank(responseBody)) {
                log.error("微信小程序发货接口调用失败，状态码：{}，响应：{}", response.getStatusCodeValue(), responseBody);
                return ERROR_CODE;
            }

            cn.hutool.json.JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
            String errcode = jsonResponse.getStr("errcode", ERROR_CODE);

            if (!SUCCESS_CODE.equals(errcode)) {
                log.error("微信小程序发货失败，错误码：{}，错误信息：{}",
                        errcode, jsonResponse.getStr("errmsg", "未知错误"));
            } else {
                log.info("微信小程序发货成功，微信支付交易号：{}", outTradeNo);
            }

            return errcode;
        } catch (Exception e) {
            log.error("微信小程序发货接口调用异常：{}", e.getMessage(), e);
            return ERROR_CODE;
        }
    }

    /**
     * 获取accessToken
     *
     * @param appid  小程序appid
     * @param secret 小程序secret
     */
    private String getAccessToken(String appid, String secret) {
        log.info("initAccessToken:开始运行...");
        String accessToken = null;
        //获取tokcen接口
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid + "&secret=" + secret;
        try {
            URL urlGet = new URL(url);
            HttpURLConnection http = (HttpURLConnection) urlGet.openConnection();
            http.setRequestMethod("GET"); // 必须是get方式请求
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setDoOutput(true);
            http.setDoInput(true);
            System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒
            System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒
            http.connect();
            InputStream is = http.getInputStream();
            int size = is.available();
            byte[] jsonBytes = new byte[size];
            is.read(jsonBytes);
            String message = new String(jsonBytes, "UTF-8");
            JSONObject demoJson = JSONObject.parseObject(message);
            accessToken = demoJson.getString("access_token");

            log.info("返回accessToken:" + accessToken);
            log.info("initAccessToken:运行结束...");
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;

    }
}
