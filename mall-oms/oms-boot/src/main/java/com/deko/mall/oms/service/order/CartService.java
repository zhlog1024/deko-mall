package com.deko.mall.oms.service.order;

import com.deko.mall.oms.model.dto.CartItemDto;

import java.util.List;

/**
 * 购物车业务服务接口
 * 提供购物车相关的核心功能，包括：
 * 1. 购物车商品管理（添加、修改、删除）
 * 2. 购物车商品查询
 * 3. 购物车商品选择状态管理
 * 4. 购物车清理
 */
public interface CartService {

    /**
     * 获取用户购物车商品列表
     *
     * @param memberId 会员ID
     * @return 购物车商品列表，包含商品信息、数量、选中状态等
     */
    List<CartItemDto> listCartItems(Long memberId);


    /**
     * 获取购物车商品总数量
     *
     * @param memberId 会员ID
     * @return 购物车商品总数量
     */
    Integer getCartCount(Long memberId);

    /**
     * 清空当前用户的购物车
     * 通常在下单完成后调用
     *
     * @return 清空结果，true表示成功
     */
    boolean deleteCart();

    /**
     * 添加商品到购物车
     * 如果商品已存在则增加指定数量
     *
     * @param skuId 商品SKU ID
     * @param count 添加数量
     * @return 购物车商品总数量
     */
    Integer addCartItem(Long skuId, Integer count);

    /**
     * 更新购物车商品信息
     * 可更新商品数量、选中状态等
     *
     * @param cartItem 购物车商品信息
     * @return 更新结果，true表示成功
     */
    boolean updateCartItem(CartItemDto cartItem);

    /**
     * 从购物车中移除指定商品
     *
     * @param skuId 商品SKU ID
     * @return 移除结果，true表示成功
     */
    boolean removeCartItem(Long skuId);

    /**
     * 移除购物车中所有已选中的商品
     * 通常在下单完成后调用
     *
     * @return 移除结果，true表示成功
     */
    boolean removeCheckedItem();

    /**
     * 设置购物车中所有商品的选中状态
     *
     * @param checked true表示全选，false表示全不选
     * @return 设置结果，true表示成功
     */
    boolean checkAll(boolean checked);

}