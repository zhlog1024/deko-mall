package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.entity.PmsShop;
import com.deko.mall.pms.model.dto.ShopUpdateDTO;
import com.deko.mall.pms.model.query.ShopPageQuery;
import com.deko.mall.pms.model.vo.ShopDetailVO;
import com.deko.mall.pms.model.vo.ShopVO;

/**
 * 店铺服务接口
 */
public interface ShopService extends IService<PmsShop> {

    /**
     * 获取店铺分页列表
     *
     * @param queryParams 查询参数
     * @return 店铺分页列表
     */
    IPage<ShopVO> getShopPages(ShopPageQuery queryParams);

    /**
     * 获取店铺详情（含店铺商品分页）
     *
     * @param shopId 店铺ID
     * @return 店铺详情
     */
    ShopDetailVO getShopDetail(Long shopId);

    /**
     * 根据供应商ID获取店铺（不存在则自动创建）
     *
     * @param supplierId 供应商ID
     * @return 店铺
     */
    PmsShop getShopBySupplierId(Long supplierId);

    /**
     * 创建店铺（供应商审核通过时调用）
     *
     * @param ownerType   归属类型(SUPPLIER-供应商, MEMBER-会员)
     * @param ownerId     归属ID
     * @param shopName    店铺名称
     * @param logo        店铺logo
     * @param description 店铺简介
     * @return 店铺
     */
    PmsShop createShop(String ownerType, Long ownerId, String shopName, String logo, String description);

    /**
     * 更新店铺信息（logo/简介/名称等）
     *
     * @param shopId      店铺ID
     * @param shopName    店铺名称
     * @param logo        店铺logo
     * @param logoLong    店铺长图
     * @param description 店铺简介
     * @param sort        排序
     * @param status      店铺状态(0-关闭,1-开启)
     * @param score       店铺评分(0-5)
     * @return 是否成功
     */
    boolean updateShopInfo(ShopUpdateDTO dto);

    /**
     * 获取供应商自己的店铺（供应商后台，不存在则自动创建）
     *
     * @param supplierId 供应商ID
     * @return 店铺VO
     */
    ShopVO getScmShopVO(Long supplierId);

    /**
     * 更新供应商自己的店铺信息（供应商后台，仅允许修改名称/logo/长图/简介）
     *
     * @param supplierId 供应商ID
     * @param dto        店铺更新参数
     * @return 是否成功
     */
    boolean updateScmShopInfo(Long supplierId, ShopUpdateDTO dto);
}
