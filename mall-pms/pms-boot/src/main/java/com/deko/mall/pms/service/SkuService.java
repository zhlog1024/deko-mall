package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.pms.model.entity.PmsSku;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.SkuInfoVO;

import java.util.List;

/**
 * 商品库存服务接口
 * <p>
 * 提供商品SKU库存相关的核心业务操作，包括：
 * 1. 库存信息查询：支持单个和批量SKU查询
 * 2. 库存状态管理：包括锁定、解锁和扣减等操作
 * 3. 订单库存处理：支持订单全生命周期的库存状态流转
 * <p>
 * 库存操作遵循以下原则：
 * 1. 原子性：所有库存操作必须是原子的，避免超卖
 * 2. 一致性：确保库存数据在各个环节保持一致
 * 3. 可追溯：所有库存变更都应该可追溯
 */
public interface SkuService extends IService<PmsSku> {

    /**
     * 获取商品库存信息
     * <p>
     * 查询指定SKU的详细信息，包括：
     * - 商品基本信息
     * - 当前库存数量
     * - 锁定库存数量
     * - 可用库存数量
     *
     * @param skuId SKU ID
     * @return 商品库存详细信息
     */
    SkuInfoDTO getSkuInfo(Long skuId);

    /**
     * 获取商品库存信息列表
     * <p>
     * 批量查询多个SKU的库存信息，用于：
     * - 购物车商品库存校验
     * - 订单创建时的库存预检
     * - 商品列表展示等场景
     *
     * @param skuIds SKU ID列表
     * @return 商品库存信息列表
     */
    List<SkuInfoDTO> listSkuInfos(List<Long> skuIds);


    /**
     * 获取商品SKU列表
     * <p>
     * 获取指定商品SKU列表，用于：
     * - 商品列表展示
     * - 商品详情展示
     * - 购物车商品展示
     * - 订单创建时获取商品信息
     *
     * @param queryParams 查询参数
     * @return 商品SKU列表
     */
    IPage<SkuInfoVO> listPagedSku(SpuPageQuery queryParams);


    /**
     * 按关键字（名称 / SKU 编码）搜索 SKU ID
     * <p>
     * 供其它模块的列表页做关键字筛选：SKU 文本属主在 pms，跨模块不直连 pms_sku。
     *
     * @param keyword 关键字（空白返回空集合）
     * @param limit   最多返回条数（&le;0 时取默认 200，上限 1000）
     * @return 命中的 SKU ID
     */
    List<Long> searchSkuIds(String keyword, Integer limit);
}
