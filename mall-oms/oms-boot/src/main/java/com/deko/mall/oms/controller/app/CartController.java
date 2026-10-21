package com.deko.mall.oms.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.oms.model.dto.CartItemDto;
import com.deko.mall.oms.service.order.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App-购物车接口
 */

@Tag(name = "App-购物车接口")
@RestController
@RequestMapping("/app-api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "查询购物车")
    @GetMapping
    public <T> Result<T> getCart() {
        List<CartItemDto> result = cartService.listCartItems(SecurityUtils.getMemberId());
        return Result.success((T) result);
    }





    @Operation(summary = "删除购物车")
    @DeleteMapping
    public <T> Result<T> deleteCart() {
        boolean result = cartService.deleteCart();
        return Result.judge(result);
    }

    @Operation(summary = "添加购物车商品")
    @PostMapping
    public Result<Integer> addCartItem(
            @RequestParam Long skuId,
            @RequestParam(required = false, defaultValue = "1") Integer count) {
        Integer cartTotal = cartService.addCartItem(skuId, count);
        return Result.success(cartTotal);
    }

    @Operation(summary = "更新购物车商品")
    @PutMapping("/skuId/{skuId}")
    public <T> Result<T> updateCartItem(
            @PathVariable Long skuId,
            @RequestBody CartItemDto cartItem
    ) {
        cartItem.setSkuId(skuId);
        boolean result = cartService.updateCartItem(cartItem);
        return Result.judge(result);
    }

    @Operation(summary = "删除购物车商品")
    @DeleteMapping("/skuId/{skuId}")
    public <T> Result<T> removeCartItem(@PathVariable Long skuId) {
        boolean result = cartService.removeCartItem(skuId);
        return Result.judge(result);
    }

    @Operation(summary = "批量删除购物车商品")
    @DeleteMapping("/batch")
    public <T> Result<T> removeCartItems(@RequestBody List<Long> skuIds) {
        for (Long skuId : skuIds) {
            cartService.removeCartItem(skuId);
        }
        return Result.success();
    }

    @Operation(summary = "查询购物车商品数量")
    @GetMapping("/count")
    public Result<Integer> getCartCount() {
        Integer count = cartService.getCartCount(SecurityUtils.getMemberId());
        return Result.success(count);
    }

    @Operation(summary = "全选/全不选购物车商品")
    @PatchMapping("/_check")
    public <T> Result<T> check(
            @Parameter(name = "全选/全不选") boolean checked
    ) {
        boolean result = cartService.checkAll(checked);
        return Result.judge(result);
    }
}
