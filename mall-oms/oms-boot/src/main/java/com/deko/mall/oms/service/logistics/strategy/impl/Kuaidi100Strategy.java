package com.deko.mall.oms.service.logistics.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.kuaidi100.sdk.api.QueryTrack;
import com.kuaidi100.sdk.api.Subscribe;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.pojo.HttpResult;
import com.kuaidi100.sdk.request.*;
import com.kuaidi100.sdk.utils.SignUtils;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.oms.config.OmsResultCode;
import com.deko.mall.oms.enums.LogisticsProviderEnum;
import com.deko.mall.oms.enums.LogisticsStatusEnum;
import com.deko.mall.oms.model.dto.LogisticsDTO;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;
import com.deko.mall.oms.service.logistics.strategy.LogisticsStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 快递100物流策略实现
 */
@Slf4j
@Component
public class Kuaidi100Strategy implements LogisticsStrategy {

    private static final int CONNECT_TIMEOUT = 6000;
    private static final int SOCKET_TIMEOUT = 6000;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Value("${logistics.kuaidi100.key}")
    private String key;
    @Value("${logistics.kuaidi100.customer}")
    private String customer;
    @Value("${logistics.kuaidi100.callback-url}")
    private String callbackUrl;
    @Value("${logistics.kuaidi100.salt}")
    private String salt;

    @Override
    public LogisticsProviderEnum getProvider() {
        return LogisticsProviderEnum.KUAIDI100;
    }

    @Override
    public List<OmsLogisticsTrack> queryTracks(String trackingNo, String carrierCode, String phone) {
        try {
            // 构建查询参数
            QueryTrackParam queryTrackParam = new QueryTrackParam();
            queryTrackParam.setCom(carrierCode);
            queryTrackParam.setNum(trackingNo);
            queryTrackParam.setPhone(phone);
            queryTrackParam.setResultv2("4");
            String param = new Gson().toJson(queryTrackParam);

            // 构建查询请求
            QueryTrackReq queryTrackReq = new QueryTrackReq();
            queryTrackReq.setParam(param);
            queryTrackReq.setCustomer(customer);
            queryTrackReq.setSign(SignUtils.querySign(param, key, customer));

            // 执行查询
            IBaseClient baseClient = new QueryTrack();
            baseClient.setTimeOut(CONNECT_TIMEOUT, SOCKET_TIMEOUT);
            HttpResult result = baseClient.execute(queryTrackReq);

            // 解析响应
            String body = result.getBody();
            if (StringUtils.isBlank(body)) {
                log.warn("快递100查询接口返回为空, trackingNo: {}", trackingNo);
                return null;
            }
            // 转换为物流轨迹对象
            return parseTrackResult(trackingNo, body);
        } catch (Exception e) {
            log.error("快递100查询接口异常, trackingNo: {}", trackingNo, e);
            throw new BizException(OmsResultCode.LOGISTICS_TRACK_QUERY_FAILED, (Object) e.getMessage());
        }
    }

    @Override
    public void subscribeTrack(LogisticsDTO subscribeDTO) {
        try {
            // 构建订阅参数
            SubscribeParameters parameters = new SubscribeParameters();
            parameters.setCallbackurl(callbackUrl);
            parameters.setPhone(subscribeDTO.getPhone());
            parameters.setSalt(salt);
            parameters.setResultv2("4");

            SubscribeParam subscribeParam = new SubscribeParam();
            subscribeParam.setParameters(parameters);
            subscribeParam.setCompany(subscribeDTO.getCarrierCode());
            if (StringUtils.isBlank(subscribeParam.getCompany())) {
                parameters.setAutoCom("1");
            }
            subscribeParam.setNumber(subscribeDTO.getTrackingNo());
            subscribeParam.setKey(key);

            // 构建订阅请求
            SubscribeReq subscribeReq = new SubscribeReq();
            subscribeReq.setSchema(ApiInfoConstant.SUBSCRIBE_SCHEMA);
            subscribeReq.setParam(new Gson().toJson(subscribeParam));

            // 执行订阅
            IBaseClient subscribe = new Subscribe();
            subscribe.setTimeOut(CONNECT_TIMEOUT, SOCKET_TIMEOUT);
            HttpResult result = subscribe.execute(subscribeReq);

            log.info("快递100订阅结果, param: {}, result: {}", JSON.toJSONString(subscribeParam), result.getBody());
        } catch (Exception e) {
            log.error("快递100订阅接口异常, trackingNo: {}", subscribeDTO.getTrackingNo(), e);
            throw new BizException(OmsResultCode.LOGISTICS_TRACK_SUBSCRIBE_FAILED, (Object) e.getMessage());
        }
    }

    @Override
    public List<OmsLogisticsTrack> handleCallback(String param, String sign) {
        if (StringUtils.isBlank(param)) {
            log.warn("快递100回调参数为空");
            return null;
        }

        try {
            log.info("收到快递100回调: param={}, sign={}", param, sign);

            // 验证签名：MD5(param + salt).toUpperCase() == sign
            String expectedSign = DigestUtils.md5Hex(param + salt).toUpperCase();

            if (!expectedSign.equals(sign)) {
                log.error("快递100回调签名验证失败, expected={}, received={}", expectedSign, sign);
                return null;
            }

            // 解析物流数据
            JSONObject lastResult = JSON.parseObject(param);
            if (lastResult == null) {
                log.warn("快递100回调数据格式错误，无法解析 param");
                return null;
            }

            // 提取物流信息
            String state = lastResult.getString("state");
            String trackingNo = lastResult.getString("nu");
            String carrierCode = lastResult.getString("com");
            JSONArray data = lastResult.getJSONArray("data");

            if (data == null || data.isEmpty()) {
                log.warn("快递100回调数据格式错误，缺少轨迹信息");
                return null;
            }

            // 解析所有轨迹事件
            List<OmsLogisticsTrack> tracks = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject event = data.getJSONObject(i);
                String context = event.getString("context");
                String timeStr = event.getString("time");
                LocalDateTime eventTime = LocalDateTime.parse(timeStr, TIME_FORMATTER);

                OmsLogisticsTrack track = new OmsLogisticsTrack();
                track.setTrackingNo(trackingNo);
                track.setStatus(convertStatus(state));
                track.setTrackEvent(context);
                track.setEventTime(eventTime);
                tracks.add(track);
            }

            log.info("解析快递100回调成功: trackingNo={}, status={}, trackCount={}",
                    trackingNo, convertStatus(state), tracks.size());

            return tracks;
        } catch (Exception e) {
            log.error("处理快递100回调异常", e);
            throw new BizException(OmsResultCode.LOGISTICS_CALLBACK_PROCESS_FAILED, (Object) e.getMessage());
        }
    }


    private List<OmsLogisticsTrack> parseTrackResult(String trackingNo, String body) {
        try {
            // 解析响应JSON
            JSONObject result = JSON.parseObject(body);
            if (result == null || !"200".equals(result.getString("status"))) {
                log.warn("快递100查询接口返回异常结果: {}", body);
                return null;
            }

            String state = result.getString("state");
            JSONArray data = result.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                return null;
            }

            // 解析所有轨迹事件
            List<OmsLogisticsTrack> tracks = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject event = data.getJSONObject(i);
                String context = event.getString("context");
                String timeStr = event.getString("time");
                LocalDateTime eventTime = LocalDateTime.parse(timeStr, TIME_FORMATTER);

                OmsLogisticsTrack track = new OmsLogisticsTrack();
                track.setTrackingNo(trackingNo);
                track.setStatus(convertStatus(event.getString("statusCode")));
                track.setTrackEvent(context);
                track.setEventTime(eventTime);
                tracks.add(track);
            }

            return tracks;
        } catch (Exception e) {
            log.error("解析快递100查询结果异常: {}", body, e);
            throw new BizException(OmsResultCode.LOGISTICS_TRACK_PARSE_FAILED, (Object) e.getMessage());
        }
    }

    /**
     * 转换快递100状态为系统状态
     */
    private Integer convertStatus(String state) {
        if (StringUtils.isBlank(state)) {
            return LogisticsStatusEnum.PENDING.getValue();
        }

        switch (state) {
            case "101":
            case "102":
                return LogisticsStatusEnum.PENDING.getValue();
            case "103":
            case "1":
                return LogisticsStatusEnum.PICKED_UP.getValue();
            case "0":
            case "1001":
            case "1002":
            case "1003":
                return LogisticsStatusEnum.IN_TRANSIT.getValue();
            case "5":
            case "501":
                return LogisticsStatusEnum.DELIVERING.getValue();
            case "3":
            case "301":
            case "302":
            case "303":
            case "304":
                return LogisticsStatusEnum.SIGNED.getValue();
            case "6":
            case "4":
            case "401":
                return LogisticsStatusEnum.RETURNING.getValue();
            case "2":
            case "7":
            case "8":
            case "10":
            case "11":
            case "12":
            case "13":
            case "14":
                return LogisticsStatusEnum.EXCEPTION.getValue();
            default:
                return LogisticsStatusEnum.PENDING.getValue();
        }
    }
}
