package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.dto.SpuDynamicDataDTO;
import com.deko.mall.pms.model.entity.PmsSpu;
import com.deko.mall.pms.model.form.PmsSpuForm;
import com.deko.mall.pms.model.form.SpuAuditForm;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.*;

import java.util.List;
import java.util.Map;

/**
 * 商品SPU业务服务接口
 * 提供商品SPU(Standard Product Unit)相关的业务功能，包括：
 * 1. 商品基础信息管理（新增、修改、删除）
 * 2. 商品列表查询（后台管理端、应用端）
 * 3. 商品详情查询（后台管理端、应用端）
 * 4. 特殊商品管理（秒杀商品等）
 */
public interface SpuService extends IService<PmsSpu> {

    /**
     * 后台管理-商品分页列表
     * 用于后台管理系统查询商品列表，包含更多管理信息
     *
     * @param queryParams 查询参数，支持按商品名称、类目、品牌、状态等条件筛选
     * @return 分页商品列表，包含商品基本信息和管理信息
     */
    IPage<PmsSpuPageVO> listPagedSpu(SpuPageQuery queryParams);

    /**
     * APP端-商品分页列表
     * 用于移动端展示商品列表，只包含用户关心的信息
     *
     * @param queryParams 查询参数，支持按商品名称、类目、品牌等条件筛选
     * @return 分页商品列表，包含商品展示信息
     */
    IPage<SpuPageVO> listPagedSpuForApp(SpuPageQuery queryParams);

    SearchResultVO listPagedSpuWithFilterOptions(SpuPageQuery queryParams);

    /**
     * 后台管理-获取商品详情
     * 获取商品的完整信息，包括基本信息、规格属性、SKU等
     *
     * @param id 商品ID
     * @return 商品详细信息，包含管理所需的所有字段
     */
    PmsSpuDetailVO getSpuDetail(Long id);

    /**
     * APP端-获取商品详情
     * 获取商品的展示信息，包括基本信息、规格属性、SKU等
     *
     * @param spuId 商品ID
     * @return 商品详细信息，包含展示所需的字段
     */
    SpuDetailVO getSpuDetailForApp(Long spuId);

    /**
     * 新增商品
     * 创建新的商品记录，包括基本信息、规格属性、SKU等
     *
     * @param formData 商品表单数据，包含商品的完整信息
     * @return 创建结果，true表示成功
     */
    boolean addSpu(PmsSpuForm formData);

    /**
     * 修改商品
     * 更新已有商品的信息，包括基本信息、规格属性、SKU等
     *
     * @param spuId    商品ID
     * @param formData 商品表单数据，包含商品的完整信息
     * @return 更新结果，true表示成功
     */
    boolean updateSpuById(Long spuId, PmsSpuForm formData);

    /**
     * 删除商品
     * 支持批量删除商品，会同时删除关联的SKU等信息
     *
     * @param ids 商品ID列表，多个以英文逗号(,)分割
     * @return 删除结果，true表示成功
     */
    boolean removeBySpuIds(String ids);

    /**
     * 获取秒杀商品列表
     * 获取当前正在秒杀的商品列表，包含秒杀价格和库存等信息
     *
     * @return 秒杀商品列表
     */
    List<SeckillingSpuVO> listSeckillingSpu();


    List<PmsSpuDTO> getProductList(List<Long> productIds);

    List<PmsSpuDTO> getProductListForApp(List<Long> productIds);

    boolean auditSpu(Long id, SpuAuditForm form);

    /**
     * 商品上架
     * 将商品状态修改为上架状态，只有审核通过的商品才能上架
     *
     * @param id 商品ID
     * @return 上架结果，true表示成功
     */
    boolean publishSpu(Long id);

    /**
     * 商品下架
     * 将商品状态修改为下架状态
     *
     * @param id 商品ID
     * @return 下架结果，true表示成功
     */
    boolean unpublishSpu(Long id);

    /**
     * 批量上架商品
     *
     * @param ids 商品ID列表
     * @return 是否成功
     */
    boolean batchPublishSpu(List<Long> ids);

    /**
     * 批量下架商品
     *
     * @param ids 商品ID列表
     * @return 是否成功
     */
    boolean batchUnpublishSpu(List<Long> ids);

    /**
     * 获取商品销量映射
     * 批量获取多个SPU的销量数据
     *
     * @param spuIds SPU ID列表
     * @return SPU销量映射，key为SPU ID，value为销量
     */
    Map<Long, Integer> getSpuSalesMap(List<Long> spuIds);

    /**
     * 获取SPU的默认SKU ID
     * 用于获取默认规格的价格和库存信息
     *
     * @param spuId SPU ID
     * @return 默认SKU ID
     */
    Long getDefaultSkuId(Long spuId);

    /**
     * 获取SKU的价格和库存信息
     * 用于批量获取多个SKU的价格、库存和上架状态等动态数据
     *
     * @param skuIds SKU ID列表
     * @return SKU信息映射，key为SKU ID，value为包含价格、库存等信息的Map
     */
    Map<Long, Map<String, Object>> getSkuInfoMap(List<Long> skuIds);

    /**
     * 获取SPU维度的商品动态数据
     * <p>
     * 批量获取SPU的动态数据，包括：
     * - 最低售价(从所有SKU中取最低价)
     * - 总库存(所有SKU库存累加)
     * - 销量(直接取SPU表中的销量)
     * - 上架状态(直接取SPU表中的状态)
     *
     * @param spuIds SPU ID列表
     * @return 动态数据映射，key格式为"s{spuId}"
     */
    Map<String, SpuDynamicDataDTO> getSpuDynamicDataMap(List<Long> spuIds);

    /**
     * 计算SKU的毛利率
     * 用于价格修改时的实时预览
     *
     * @param skuVO SKU视图对象
     */
    void calculateMargins(PmsSkuVO skuVO);



    /**
     * 计算SKU零售毛利率（兼容旧接口名）
     *
     * @param skuVO SKU视图对象
     */
    void calculateSkuPrice(PmsSkuVO skuVO);

    /**
     * 计算SKU毛利率
     * 计算SKU零售毛利率
     *
     * @param skuVO SKU视图对象
     */
    void calculateSkuMargin(PmsSkuVO skuVO);

    /**
     * 计算SPU的完整定价信息（包括SKU价格、毛利率、商品类型）
     * 
     * @param spuId SPU ID
     * @param skuList SKU列表（包含零售价、供货价）
     * @return 计算结果
     */
    SpuCalculateResultVO calculateSpuPricingInfo(Long spuId, java.util.List<PmsSkuVO> skuList, String spuType);

}
