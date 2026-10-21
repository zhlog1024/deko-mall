package com.deko.mall.oms.service.logistics.strategy.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.deko.mall.oms.enums.LogisticsProviderEnum;
import com.deko.mall.oms.model.dto.LogisticsDTO;
import com.deko.mall.oms.model.entity.logistics.OmsLogisticsTrack;
import com.deko.mall.oms.service.logistics.strategy.LogisticsStrategy;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 微信小程序物流策略实现
 */
@Slf4j
@Component("wx_miniapp")
@RequiredArgsConstructor
public class WxMiniappLogisticsStrategy implements LogisticsStrategy {

    private final RestTemplate restTemplate;

    @Value("${wx.miniapp.appId}")
    private String appId;

    @Value("${wx.miniapp.appSecret}")
    private String secret;

    @Override
    public LogisticsProviderEnum getProvider() {
        return LogisticsProviderEnum.WX_MINIAPP;
    }

    @Override
    public void subscribeTrack(LogisticsDTO logisticsDTO) {
        // 仅微信支付订单需要上报物流信息
        if (!PayChannelEnum.WECHAT_JSAPI.getValue().equals(logisticsDTO.getPayChannel())) {
            log.debug("非微信支付订单，跳过物流报备: bizNo={}, payChannel={}",
                    logisticsDTO.getBizNo(), logisticsDTO.getPayChannel());
            return;
        }

        try {
            String accessToken = getAccessToken();

            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info?access_token=" + accessToken;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            JSONObject orderKey = new JSONObject();
            // 订单单号类型，用于确认需要上传详情的订单。枚举值1，使用下单商户号和商户侧单号；枚举值2，使用微信支付单号。
            orderKey.put("order_number_type", 2);
            orderKey.put("transaction_id", logisticsDTO.getChannelTradeNo());

            JSONObject shipping = new JSONObject();
            //商品信息
            shipping.put("item_desc", logisticsDTO.getSpuName());
            //物流单号，物流快递发货时必填
            shipping.put("tracking_no", logisticsDTO.getTrackingNo());
            // 快递公司编码，物流快递发货时必填
            shipping.put("express_company", logisticsDTO.getCarrierCode());


            JSONObject contact = new JSONObject();
            contact.put("receiver_contact", logisticsDTO.getPhone());
            shipping.put("contact", contact);

            JSONObject payer = new JSONObject();
            payer.put("openid", logisticsDTO.getOpenid());

            JSONObject signObject = new JSONObject();
            JSONArray shipping_list = new JSONArray();
            shipping_list.add(shipping);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            String uploadTime = simpleDateFormat.format(System.currentTimeMillis());

            signObject.put("order_key", orderKey);
            // 发货模式，发货模式枚举值：1、UNIFIED_DELIVERY（统一发货）2、SPLIT_DELIVERY（分拆发货） 示例值: 1
            signObject.put("delivery_mode", 1);
            // 物流模式，发货方式枚举值：1、实体物流配送采用快递公司进行实体物流配送形式 2、同城配送 3、虚拟商品，虚拟商品，例如话费充值，点卡等，无实体配送形式 4、用户自提
            signObject.put("logistics_type", 1);
            signObject.put("shipping_list", shipping_list);
            signObject.put("upload_time", uploadTime);
            signObject.put("payer", payer);
            log.info("发货信息：{}", signObject);


            String requestBody = JSONObject.toJSONString(signObject);
            headers.setContentLength(requestBody.getBytes(StandardCharsets.UTF_8).length);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("微信小程序物流订阅失败，orderNo={}, response={}", logisticsDTO.getBizNo(), response.getBody());
            }

            JSONObject responseBody = JSONObject.parseObject(response.getBody());
            if (responseBody.getIntValue("errcode") != 0) {
                log.error("微信小程序物流订阅失败，orderNo={}, errcode={}, errmsg={}",
                        logisticsDTO.getBizNo(), responseBody.getInteger("errcode"), responseBody.getString("errmsg"));
            }
        } catch (Exception e) {
            log.error("微信小程序物流订阅失败，orderNo={}, errcode={}, errmsg={}",
                    logisticsDTO.getBizNo(), e.getMessage());
        }

    }

    @Override
    public List<OmsLogisticsTrack> queryTracks(String trackingNo, String carrierCode, String phone) {
        return List.of();
    }

    @Override
    public List<OmsLogisticsTrack> handleCallback(String param, String sign) {
        return List.of();
    }

    private String getAccessToken() {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, secret);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = JSONObject.parseObject(response.getBody());
            return jsonObject.getString("access_token");
        }
        return null;
    }
}
