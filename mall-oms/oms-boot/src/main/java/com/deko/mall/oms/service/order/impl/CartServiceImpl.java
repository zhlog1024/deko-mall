package com.deko.mall.oms.service.order.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.oms.config.OmsResultCode;
import com.deko.mall.oms.constant.OrderConstants;
import com.deko.mall.oms.converter.CartConverter;
import com.deko.mall.oms.model.dto.CartItemDto;
import com.deko.mall.oms.service.order.CartService;
import com.deko.mall.pms.api.SkuFeignClient;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


/**
 * 购物车模块
 * <p>
 * 核心技术：BoundHashOperations
 * 数据格式：
 * -- key <--> 商品列表
 * hKey:value <--> skuId 商品1
 * hKey:value <--> 商品2
 * hKey:value <--> 商品3
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate redisTemplate;
    private final SkuFeignClient skuFeignService;
    private final CartConverter cartConverter;

    @Override
    public List<CartItemDto> listCartItems(Long memberId) {
        if (memberId == null) {
            return Collections.EMPTY_LIST;
        }
        BoundHashOperations<String, String, CartItemDto> cartHashOperations = getCartHashOperations(memberId);
        List<CartItemDto> cartItems = cartHashOperations.values();
        if (CollectionUtil.isEmpty(cartItems)) {
            return Collections.EMPTY_LIST;
        }
        // 获取 cartItems 去除 skuId，组装成 ids
        List<Long> skuIds = cartItems.stream().map(CartItemDto::getSkuId).toList();
        Result<List<SkuInfoDTO>> skuInfoListResult = skuFeignService.getSkuInfoList(skuIds);
        List<SkuInfoDTO> skuInfoDTO = skuInfoListResult.getData();
        // 遍历 cartItems，把 sku 信息填充进去
        cartItems.forEach(cartItem -> {
            SkuInfoDTO skuInfo = skuInfoDTO.stream().filter(s -> s.getId().equals(cartItem.getSkuId())).findFirst().orElse(null);
            if (null != skuInfo) {
                cartItem.setSkuSn(skuInfo.getSkuSn());
                cartItem.setSkuName(skuInfo.getSkuName());
                cartItem.setPicUrl(skuInfo.getPicUrl());
                cartItem.setSpuId(skuInfo.getSpuId());
                cartItem.setSpuName(skuInfo.getSpuName());
                cartItem.setStock(skuInfo.getStock());
                cartItem.setPrice(skuInfo.getRetailPrice());
            }
        });
        return cartItems;
    }

    @Override
    public Integer getCartCount(Long memberId) {
        if (memberId == null) {
            return 0;
        }
        int count = 0;
        for (CartItemDto item : ((List<CartItemDto>) getCartHashOperations(memberId).values())) {
            count += item.getCount() != null ? item.getCount() : 0;
        }
        return count;
    }

    /**
     * 删除用户购物车(清空购物车)
     */
    @Override
    public boolean deleteCart() {
        String key = OrderConstants.MEMBER_CART_PREFIX + SecurityUtils.getMemberId();
        redisTemplate.delete(key);
        return true;
    }

    /**
     * 添加商品至购物车
     */
    @Override
    public Integer addCartItem(Long skuId, Integer count) {
        Long memberId = SecurityUtils.getMemberId();
        BoundHashOperations<String, String, CartItemDto> cartHashOperations = getCartHashOperations(memberId);
        String hKey = String.valueOf(skuId);

        // 确保 count 有效
        int addCount = (count != null && count > 0) ? count : 1;

        CartItemDto cartItem = cartHashOperations.get(hKey);

        if (cartItem != null) {
            // 购物车已存在该商品，叠加用户选择的数量
            cartItem.setCount(cartItem.getCount() + addCount);
            cartItem.setChecked(true);
        } else {
            // 购物车中不存在该商品，使用用户选择的数量新增
            Result<SkuInfoDTO> skuInfoDTOResult = skuFeignService.getSkuInfo(skuId);
            SkuInfoDTO skuInfo = skuInfoDTOResult.getData();
            if (skuInfo != null) {
                cartItem = cartConverter.sku2CartItem(skuInfo);
                cartItem.setCount(addCount);
                cartItem.setChecked(true);
            }
        }
        cartHashOperations.put(hKey, cartItem);
        return getCartCount(memberId);
    }

    /**
     * 更新购物车总商品数量、选中状态
     */
    @Override
    public boolean updateCartItem(CartItemDto cartItem) {
        Long memberId;
        try {
            memberId = SecurityUtils.getMemberId();
        } catch (Exception e) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
        BoundHashOperations cartHashOperations = getCartHashOperations(memberId);
        String hKey = cartItem.getSkuId() + "";
        if (cartHashOperations.get(hKey) != null) {
            CartItemDto cacheCartItem = (CartItemDto) cartHashOperations.get(hKey);
            if (cartItem.getChecked() != null) {
                cacheCartItem.setChecked(cartItem.getChecked());
            }
            if (cartItem.getCount() != null) {
                cacheCartItem.setCount(cartItem.getCount());
            }
            cartHashOperations.put(hKey, cacheCartItem);
        }
        return true;
    }

    /**
     * 移除购物车的商品
     */
    @Override
    public boolean removeCartItem(Long skuId) {
        Long memberId;
        try {
            memberId = SecurityUtils.getMemberId();
        } catch (Exception e) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
        BoundHashOperations cartHashOperations = getCartHashOperations(memberId);
        String hKey = skuId + "";
        cartHashOperations.delete(hKey);
        return true;
    }


    /**
     * 设置商品全选
     */
    @Override
    public boolean checkAll(boolean checked) {
        Long memberId;
        try {
            memberId = SecurityUtils.getMemberId();
        } catch (Exception e) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
        BoundHashOperations cartHashOperations = getCartHashOperations(memberId);
        for (Object value : cartHashOperations.values()) {
            CartItemDto cartItem = (CartItemDto) value;
            cartItem.setChecked(checked);
            String hKey = cartItem.getSkuId() + "";
            cartHashOperations.put(hKey, cartItem);
        }
        return true;
    }






    /**
     * 移除购物车选中的商品，作用场景：
     * <p>
     * 1.支付后删除购物车的商品
     */
    @Override
    public boolean removeCheckedItem() {
        Long memberId = SecurityUtils.getMemberId();
        if (memberId == null) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }
        BoundHashOperations cartHashOperations = getCartHashOperations(memberId);
        for (Object value : cartHashOperations.values()) {
            CartItemDto cartItem = (CartItemDto) value;
            if (cartItem.getChecked()) {
                cartHashOperations.delete(cartItem.getSkuId() + "");
            }
        }
        return true;
    }
    /**
     * 获取购物车 Hash
     */
    @SuppressWarnings("unchecked")
    private BoundHashOperations<String, String, CartItemDto> getCartHashOperations(Long memberId) {
        String cartKey = OrderConstants.MEMBER_CART_PREFIX + memberId;
        return redisTemplate.boundHashOps(cartKey);
    }
}
