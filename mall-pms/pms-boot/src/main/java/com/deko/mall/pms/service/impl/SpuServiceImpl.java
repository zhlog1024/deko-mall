package com.deko.mall.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.base.IBaseEnum;
import com.deko.mall.common.enums.SystemTypeEnum;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.constant.ProductConstants;
import com.deko.mall.pms.converter.SkuConverter;
import com.deko.mall.pms.converter.SpuAttributeConverter;
import com.deko.mall.pms.converter.SpuConverter;
import com.deko.mall.pms.enums.AttributeTypeEnum;
import com.deko.mall.pms.enums.SpuStatusEnum;
import com.deko.mall.pms.enums.SpuTypeEnum;
import com.deko.mall.pms.mapper.PmsShopMapper;
import com.deko.mall.pms.mapper.PmsSpuMapper;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.dto.SpuDynamicDataDTO;
import com.deko.mall.pms.model.entity.PmsBrand;
import com.deko.mall.pms.model.entity.PmsShop;
import com.deko.mall.pms.service.BrandService;
import com.deko.mall.pms.model.entity.PmsSku;
import com.deko.mall.pms.model.entity.PmsSpu;
import com.deko.mall.pms.model.entity.PmsSpuAttribute;
import com.deko.mall.pms.model.form.PmsSpuAttributeForm;
import com.deko.mall.pms.model.form.PmsSpuForm;
import com.deko.mall.pms.model.form.SpuAuditForm;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.*;
import com.deko.mall.pms.service.SkuService;
import com.deko.mall.pms.service.SpuAttributeService;
import com.deko.mall.pms.service.SpuService;
import com.deko.mall.common.rabbitmq.constant.MqBizConstants;
import com.deko.mall.common.rabbitmq.dto.SystemNotifyMessage;
import com.deko.mall.common.rabbitmq.dto.MessageSendDTO;
import com.deko.mall.common.rabbitmq.service.MessageSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品业务实现类
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class SpuServiceImpl extends ServiceImpl<PmsSpuMapper, PmsSpu> implements SpuService {

    private final SkuService skuService;
    private final SpuAttributeService spuAttributeService;
    private final SpuConverter spuConverter;
    private final SpuAttributeConverter spuAttributeConverter;
    private final SkuConverter skuConverter;
    private final BrandService brandService;

    // 设置nacos 字段 是否可以购买
    @Value("${pms.buy.can}")
    private Boolean canBuy;
    // 毛利率告警配置 - 价格异常预警（零售毛利率）
    @Value("${pms.margin-alert.enabled:false}")
    private Boolean marginAlertEnabled;
    @Value("${pms.margin-alert.anomaly.retail-margin-min:5.0}")
    private Double anomalyRetailMarginMin;

    private final MessageSendService messageSendService;
    private final PmsShopMapper pmsShopMapper;

    /**
     * Admin-商品分页列表
     *
     * @param queryParams 查询参数
     * @return 商品分页列表 IPage<PmsSpuPageVO>
     */
    @Override
    public IPage<PmsSpuPageVO> listPagedSpu(SpuPageQuery queryParams) {
        Page<PmsSpuPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());
        List<PmsSpuPageVO> list = this.baseMapper.listPagedSpu(page, queryParams);

        // 批量查询所有商品的SKU列表
        if (list != null && !list.isEmpty()) {
            List<Long> spuIds = list.stream().map(PmsSpuPageVO::getId).collect(Collectors.toList());

            // 构建SKU查询条件
            LambdaQueryWrapper<PmsSku> skuQueryWrapper = new LambdaQueryWrapper<PmsSku>()
                    .in(PmsSku::getSpuId, spuIds)
                    .eq(PmsSku::getDeleted, 0);

            // 如果有供应商ID，则只查询该供应商的SKU和无供应商的旧数据
            if (queryParams.getSupplierId() != null) {
                skuQueryWrapper.and(w -> w.eq(PmsSku::getSupplierId, queryParams.getSupplierId())
                        .or().isNull(PmsSku::getSupplierId));
            }

            List<PmsSku> skuList = skuService.list(skuQueryWrapper);

            // 转换为VO并计算零售毛利率
            List<PmsSkuVO> skuVOList = skuConverter.entity2VOList(skuList);
            skuVOList.forEach(this::calculateMargins);

            // 将SKU按SPU分组
            Map<Long, List<PmsSkuVO>> skuMap = skuVOList.stream()
                    .collect(Collectors.groupingBy(PmsSkuVO::getSpuId));

            // 为每个商品设置 SKU 列表
            list.forEach(item -> {
                List<PmsSkuVO> spuSkus = skuMap.get(item.getId());
                if (spuSkus != null) {
                    item.setSkuList(spuSkus);
                }
                // 处理商品类型描述
                if (item.getSpuType() != null) {
                    item.setSpuTypeDesc(IBaseEnum.getLabelByValue(item.getSpuType(), SpuTypeEnum.class));
                }
            });
        }
        page.setRecords(list);
        return page;
    }

    /**
     * APP-商品分页列表
     *
     * @param queryParams 查询参数
     * @return 商品分页列表 IPage<SpuPageVO>
     */
    @Override
    public IPage<SpuPageVO> listPagedSpuForApp(SpuPageQuery queryParams) {
        Page<SpuPageVO> page = new Page<>(queryParams.getPageNum(), queryParams.getPageSize());

        String keyWords = URLDecoder.decode(queryParams.getKeywords(), CharsetUtil.charset("UTF-8"), false);

        if (StrUtil.isBlank(keyWords)) {
            queryParams.setKeywords(null);
        } else {
            queryParams.setKeywords(keyWords);
        }
        // 仅保留普通商品
        List<String> allowTypes = Arrays.asList(
                SpuTypeEnum.NORMAL.getValue()
        );
        queryParams.setSpuTypes(allowTypes);
        List<SpuPageVO> list = this.baseMapper.listPagedSpuForApp(page, queryParams);
        applyRoleBasedPricing(list);
        // 处理商品类型描述
        list.forEach(item -> {
            if (item.getSpuType() != null) {
                item.setSpuTypeDesc(IBaseEnum.getLabelByValue(item.getSpuType(), SpuTypeEnum.class));
            }
        });
        page.setRecords(list);
        return page;
    }

    @Override
    public SearchResultVO listPagedSpuWithFilterOptions(SpuPageQuery queryParams) {
        // 1. 查询商品分页列表
        IPage<SpuPageVO> page = listPagedSpuForApp(queryParams);
        SearchResultVO result = new SearchResultVO();
        result.setList(page.getRecords());
        result.setTotal(page.getTotal());

        // 2. 查询搜索结果中出现的品牌
        String keywords = queryParams.getKeywords();
        Long categoryId = queryParams.getCategoryId();
        List<Long> brandIds = this.baseMapper.selectDistinctBrandIds(keywords, categoryId);
        if (brandIds != null && !brandIds.isEmpty()) {
            List<PmsBrand> brands = brandService.list(new LambdaQueryWrapper<PmsBrand>()
                    .in(PmsBrand::getId, brandIds)
                    .select(PmsBrand::getId, PmsBrand::getName, PmsBrand::getLogoUrl)
                    .orderByAsc(PmsBrand::getSort));
            result.setBrandList(brands.stream().map(b -> {
                SearchResultVO.BrandOptionVO vo = new SearchResultVO.BrandOptionVO();
                vo.setId(b.getId());
                vo.setName(b.getName());
                vo.setLogoUrl(b.getLogoUrl());
                return vo;
            }).collect(Collectors.toList()));
        }

        // 3. 价格区间（后续可改为数据库动态配置）
        List<SearchResultVO.PriceRangeVO> priceRanges = new java.util.ArrayList<>();
        long[][] ranges = {{0, 50}, {50, 100}, {100, 200}, {200, 500}, {500, 0}};
        String[] names = {"0-50", "50-100", "100-200", "200-500", "500以上"};
        for (int i = 0; i < ranges.length; i++) {
            SearchResultVO.PriceRangeVO pr = new SearchResultVO.PriceRangeVO();
            pr.setName(names[i]);
            pr.setMinPrice(ranges[i][0]);
            pr.setMaxPrice(ranges[i][1]);
            priceRanges.add(pr);
        }
        result.setScreeningPriceList(priceRanges);

        return result;
    }

    /**
     * 统一按零售价展示
     */
    private void applyRoleBasedPricing(List<SpuPageVO> list) {
        return;
    }

    /**
     * App-获取商品详情
     *
     * @param spuId 商品ID
     * @return 商品详情
     */
    @Override
    public SpuDetailVO getSpuDetailForApp(Long spuId) {
        PmsSpu pmsSpu = this.getById(spuId);
        if (pmsSpu == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        //判断商品上下架状态
        if (!pmsSpu.getStatus().equals(SpuStatusEnum.ON_SALE.getValue())) {
            throw new IllegalArgumentException("商品已下架");
        }
        // 店铺关闭的商品不可访问
        if (pmsSpu.getSupplierId() != null) {
            PmsShop pmsShop = pmsShopMapper.selectOne(new LambdaQueryWrapper<PmsShop>()
                    .eq(PmsShop::getOwnerType, "SUPPLIER")
                    .eq(PmsShop::getOwnerId, pmsSpu.getSupplierId())
                    .last("LIMIT 1"));
            if (pmsShop == null || pmsShop.getStatus() == null || pmsShop.getStatus() != 1) {
                throw new IllegalArgumentException("店铺已关闭");
            }
        }

        SpuDetailVO spuDetailVO = new SpuDetailVO();

        // 商品基本信息
        SpuDetailVO.GoodsInfo goodsInfo = new SpuDetailVO.GoodsInfo();
        BeanUtil.copyProperties(pmsSpu, goodsInfo, "album");
        // 普通商品均支持加入购物车
        goodsInfo.setCanAddCart(true);

        List<String> album = new ArrayList<>();

        if (StrUtil.isNotBlank(pmsSpu.getPicUrl())) {
            album.add(pmsSpu.getPicUrl());
        }
        if (pmsSpu.getAlbum() != null && pmsSpu.getAlbum().length > 0) {
            album.addAll(Arrays.asList(pmsSpu.getAlbum()));
        }
        // Always set album (picUrl first if present, then sub pics)
        goodsInfo.setAlbum(album);
        goodsInfo.setCanBuy(canBuy);
        spuDetailVO.setGoodsInfo(goodsInfo);

        // 商品属性列表
        List<SpuDetailVO.Attribute> attributeList = spuAttributeService.list(new LambdaQueryWrapper<PmsSpuAttribute>()
                        .eq(PmsSpuAttribute::getType, AttributeTypeEnum.ATTR.getValue()).eq(PmsSpuAttribute::getSpuId, spuId)
                        .select(PmsSpuAttribute::getId, PmsSpuAttribute::getName, PmsSpuAttribute::getValue)).stream().
                map(item -> {
                    SpuDetailVO.Attribute attribute = new SpuDetailVO.Attribute();
                    BeanUtil.copyProperties(item, attribute);
                    return attribute;
                }).collect(Collectors.toList());
        spuDetailVO.setAttributeList(attributeList);

        // 商品规格列表
        List<PmsSpuAttribute> specSourceList = spuAttributeService.list(new LambdaQueryWrapper<PmsSpuAttribute>()
                .eq(PmsSpuAttribute::getType, AttributeTypeEnum.SPEC.getValue())
                .eq(PmsSpuAttribute::getSpuId, spuId)
                .select(PmsSpuAttribute::getId, PmsSpuAttribute::getName, PmsSpuAttribute::getValue));

        List<SpuDetailVO.Specification> specList = new ArrayList<>();
        // 规格Map [key:"颜色",value:[{id:1,value:"黑"},{id:2,value:"白"}]]
        Map<String, List<PmsSpuAttribute>> specValueMap = specSourceList.stream().collect(Collectors.groupingBy(PmsSpuAttribute::getName));

        for (Map.Entry<String, List<PmsSpuAttribute>> entry : specValueMap.entrySet()) {
            String specName = entry.getKey();
            List<PmsSpuAttribute> specValueSourceList = entry.getValue();

            // 规格映射处理
            SpuDetailVO.Specification spec = new SpuDetailVO.Specification();
            spec.setName(specName);
            if (CollectionUtil.isNotEmpty(specValueSourceList)) {
                List<SpuDetailVO.Specification.Value> specValueList = specValueSourceList.stream().map(item -> {
                    SpuDetailVO.Specification.Value specValue = new SpuDetailVO.Specification.Value();
                    specValue.setId(item.getId());
                    specValue.setValue(item.getValue());
                    return specValue;
                }).collect(Collectors.toList());
                spec.setValues(specValueList);
                specList.add(spec);
            }
        }
        spuDetailVO.setSpecList(specList);

        // 商品SKU列表
        List<PmsSku> skuSourceList = skuService.list(new LambdaQueryWrapper<PmsSku>().eq(PmsSku::getSpuId, spuId));
        if (CollectionUtil.isNotEmpty(skuSourceList)) {
            List<SpuDetailVO.Sku> skuList = skuSourceList.stream().map(item -> {
                SpuDetailVO.Sku sku = new SpuDetailVO.Sku();
                BeanUtil.copyProperties(item, sku);
                sku.setPrice(item.getRetailPrice());
                return sku;
            }).collect(Collectors.toList());
            spuDetailVO.setSkuList(skuList);
        }

        // 店铺信息（商品所属供应商店铺
        if (pmsSpu.getSupplierId() != null) {
            PmsShop pmsShop = pmsShopMapper.selectOne(new LambdaQueryWrapper<PmsShop>()
                    .eq(PmsShop::getOwnerType, "SUPPLIER")
                    .eq(PmsShop::getOwnerId, pmsSpu.getSupplierId())
                    .last("LIMIT 1"));
            if (pmsShop != null && pmsShop.getStatus() != null && pmsShop.getStatus() == 1) {
                SpuDetailVO.ShopInfo shopInfo = new SpuDetailVO.ShopInfo();
                shopInfo.setId(pmsShop.getId());
                shopInfo.setName(pmsShop.getShopName());
                shopInfo.setLogo(pmsShop.getLogo());
                shopInfo.setDescribe(pmsShop.getDescription());
                shopInfo.setIconList(new ArrayList<>());
                shopInfo.setUrl("/pages/plugins/shop/detail/detail?id=" + pmsShop.getId());
                spuDetailVO.setPluginsShopData(shopInfo);
            }
        }

        return spuDetailVO;
    }

    /**
     * 获取商品详情
     *
     * @param spuId 商品ID
     * @return 商品详情
     */
    @Override
    public PmsSpuDetailVO getSpuDetail(Long spuId) {
        PmsSpuDetailVO pmsSpuDetailVO = new PmsSpuDetailVO();

        PmsSpu entity = this.getById(spuId);
        if (entity == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        BeanUtil.copyProperties(entity, pmsSpuDetailVO, "album");
        pmsSpuDetailVO.setSubPicUrls(entity.getAlbum());

        //增加商品类型描述
        pmsSpuDetailVO.setSpuTypeDesc(IBaseEnum.getLabelByValue(entity.getSpuType(), SpuTypeEnum.class));

        // 商品属性列表
        List<PmsSpuAttribute> attrList = spuAttributeService.list(new LambdaQueryWrapper<PmsSpuAttribute>()
                .eq(PmsSpuAttribute::getSpuId, spuId)
                .eq(PmsSpuAttribute::getType, AttributeTypeEnum.ATTR.getValue()));
        pmsSpuDetailVO.setAttrList(attrList);

        // 商品规格列表
        List<PmsSpuAttribute> specList = spuAttributeService.list(new LambdaQueryWrapper<PmsSpuAttribute>()
                .eq(PmsSpuAttribute::getSpuId, spuId)
                .eq(PmsSpuAttribute::getType, AttributeTypeEnum.SPEC.getValue()));
        pmsSpuDetailVO.setSpecList(specList);

        // 商品SKU列表 - 根据SPU的供应商ID过滤
        Long spuSupplierId = entity.getSupplierId();
        LambdaQueryWrapper<PmsSku> skuQueryWrapper = new LambdaQueryWrapper<PmsSku>().eq(PmsSku::getSpuId, spuId);
        if (spuSupplierId != null) {
            // SPU有供应商ID时，只返回该供应商的SKU以及supplierId为null的旧数据
            skuQueryWrapper.and(w -> w.eq(PmsSku::getSupplierId, spuSupplierId).or().isNull(PmsSku::getSupplierId));
        }
        List<PmsSku> skuList = skuService.list(skuQueryWrapper);
        List<PmsSkuVO> skuVOList = skuConverter.entity2VOList(skuList);
        // 计算零售毛利率
        skuVOList.forEach(skuVO -> {
            calculateMargins(skuVO);
        });
        pmsSpuDetailVO.setSkuList(skuVOList);
        return pmsSpuDetailVO;

    }

    /**
     * 添加商品
     *
     * @param formData 商品表单
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean addSpu(PmsSpuForm formData) {

        // 设置供应商ID
        if (formData.getSupplierId() == null) {
            formData.setSupplierId(SecurityUtils.getSupplierId());
        }
        // 设置店铺ID（供应商→店铺映射）
        if (formData.getShopId() == null && formData.getSupplierId() != null) {
            formData.setShopId(findShopIdBySupplierId(formData.getSupplierId()));
        }

        PmsSpu entity = spuConverter.form2Entity(formData);

        entity.setStatus(SpuStatusEnum.DRAFT.getValue());
        entity.setSubmitTime(LocalDateTime.now());
        entity.setRealSales(0);
        entity.setVirtualSales(0);
        entity.setSales(0);

        boolean result = this.save(entity);
        if (result) {
            Long spuId = entity.getId();
            Long supplierId = entity.getSupplierId();
            // 保存属性
            List<PmsSpuAttributeForm> attrList = formData.getAttrList();
            this.saveSpuAttrs(spuId, attrList);
            // 保存规格
            List<PmsSpuAttributeForm> specList = formData.getSpecList();
            Map<String, Long> tempWithNewSpecIdMap = this.saveSpuSpecs(spuId, specList);
            // 保存SKU
            List<PmsSku> pmsSkus = skuConverter.form2ListEntity(formData.getSkuList());
            this.saveSku(spuId, pmsSkus, tempWithNewSpecIdMap, supplierId);
        }
        // 无异常返回true
        return result;
    }

    /**
     * 修改商品
     *
     * @param spuId    商品ID
     * @param formData 商品表单
     * @return 是否成功
     */
    @Transactional
    @Override
    public boolean updateSpuById(Long spuId, PmsSpuForm formData) {

        // 供应商变更时同步刷新店铺ID
        if (formData.getSupplierId() != null && formData.getShopId() == null) {
            formData.setShopId(findShopIdBySupplierId(formData.getSupplierId()));
        }

        PmsSpu entity = spuConverter.form2Entity(formData);

        boolean result = this.updateById(entity);
        if (result) {
            // 获取SPU的供应商ID
            Long supplierId = this.getById(spuId).getSupplierId();

            // 属性保存
            List<PmsSpuAttributeForm> attrList = formData.getAttrList();
            this.saveSpuAttrs(spuId, attrList);

            // 保存商品规格值
            List<PmsSpuAttributeForm> specList = formData.getSpecList();
            Map<String, Long> specTempIdIdMap = this.saveSpuSpecs(spuId, specList);

            // SKU保存
            List<PmsSku> skuList = skuConverter.form2ListEntity(formData.getSkuList());
            this.saveSku(spuId, skuList, specTempIdIdMap, supplierId);
        }

        return result;
    }

    /**
     * 删除商品
     *
     * @param ids 商品ID，多个以英文逗号(,)分割
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean removeBySpuIds(String ids) {

        String[] spuIds = ids.split(",");

        for (String spuId : spuIds) {
            skuService.remove(new LambdaQueryWrapper<PmsSku>().eq(PmsSku::getSpuId, spuId));
            // 规格
            spuAttributeService.remove(new LambdaQueryWrapper<PmsSpuAttribute>().eq(PmsSpuAttribute::getSpuId, spuId));
            // 属性
            spuAttributeService.remove(new LambdaQueryWrapper<PmsSpuAttribute>().eq(PmsSpuAttribute::getSpuId, spuId));
            // SPU
            this.removeById(spuId);
        }
        // 无异常直接返回true
        return true;
    }

    /**
     * 获取商品秒杀接口
     *
     * @return 商品秒杀列表
     */
    @Override
    public List<SeckillingSpuVO> listSeckillingSpu() {
        List<PmsSpu> entities = this.list(new LambdaQueryWrapper<PmsSpu>()
                //.select(PmsSpu::getId, PmsSpu::getName, PmsSpu::getPicUrl, PmsSpu::getPrice)
                .orderByDesc(PmsSpu::getCreateTime)
        );
        return spuConverter.entity2SeckillingVO(entities);
    }
    @Override
    public List<PmsSpuDTO> getProductList(List<Long> productIds) {
        // 调用Mapper查询，price和originPrice从SKU取最低价
        List<Map<String, Object>> result = baseMapper.listSpuWithMinPrice(productIds);

        // 转换为DTO
        return result.stream().map(map -> {
            PmsSpuDTO dto = new PmsSpuDTO();
            dto.setId((Long) map.get("id"));
            dto.setName((String) map.get("name"));
            dto.setCategoryId(map.get("category_id") != null ? ((Number) map.get("category_id")).longValue() : null);
            dto.setBrandId(map.get("brand_id") != null ? ((Number) map.get("brand_id")).longValue() : null);
            dto.setPicUrl((String) map.get("pic_url"));
            dto.setPrice(map.get("price") != null ? new BigDecimal(map.get("price").toString()) : null);
            dto.setOriginalPrice(map.get("origin_price") != null ? new BigDecimal(map.get("origin_price").toString()) : null);
            dto.setTotalStock(map.get("total_stock") != null ? ((Number) map.get("total_stock")).intValue() : 0);
            dto.setSales(map.get("sales") != null ? ((Number) map.get("sales")).intValue() : 0);
            dto.setRealSales(map.get("real_sales") != null ? ((Number) map.get("real_sales")).intValue() : 0);
            dto.setVirtualSales(map.get("virtual_sales") != null ? ((Number) map.get("virtual_sales")).intValue() : 0);
            dto.setUnit((String) map.get("unit"));
            dto.setDetail((String) map.get("detail"));
            dto.setDescription((String) map.get("description"));
            dto.setSpuType((String) map.get("spu_type"));
            dto.setSpuTypeDesc((String) map.get("spu_type_desc"));
            dto.setStatus(map.get("status") != null ? ((Number) map.get("status")).intValue() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PmsSpuDTO> getProductListForApp(List<Long> productIds) {
        List<PmsSpuDTO> list = getProductList(productIds);
        applyRoleBasedPricingForDto(list);
        return list;
    }

    /** 统一按零售价展示 */
    private void applyRoleBasedPricingForDto(List<PmsSpuDTO> list) {
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditSpu(Long id, SpuAuditForm form) {
        PmsSpu spu = this.getById(id);
        if (spu == null) {
            return false;
        }
        // 设置审核结果
        spu.setAuditStatus(form.getAuditStatus());
        spu.setAuditTime(LocalDateTime.now());
        spu.setAuditUser(form.getAuditUser());
        spu.setRemark(form.getRemark());

        return updateById(spu);
    }

    /**
     * 商品上架
     * 将商品状态修改为上架状态，只有审核通过的商品才能上架
     *
     * @param id 商品ID
     * @return 上架结果，true表示成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishSpu(Long id) {
        PmsSpu spu = this.getById(id);
        if (spu == null) {
            return false;
        }

        // 验证商品是否审核通过
        if (spu.getAuditStatus() != 1) {
            throw new IllegalStateException("只有审核通过的商品才能上架");
        }

        // 设置商品状态为上架(1)
        spu.setStatus(SpuStatusEnum.ON_SALE.getValue());
        return this.updateById(spu);
    }

    /**
     * 商品下架
     * 将商品状态修改为下架状态
     *
     * @param id 商品ID
     * @return 下架结果，true表示成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unpublishSpu(Long id) {
        // 先查询商品是否存在
        PmsSpu spu = this.getById(id);
        if (spu == null) {
            return false;
        }

        // 可选：检查商品当前状态
        // if (spu.getStatus() != SpuStatusEnum.ON_SALE.getValue()) {
        //    throw new IllegalStateException("只有已上架的商品才能下架");
        // }

        // 设置商品状态为下架(0)
        spu.setStatus(SpuStatusEnum.OFF_SALE.getValue());
        return this.updateById(spu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchPublishSpu(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 先批量查询商品是否存在
        List<PmsSpu> spus = this.listByIds(ids);
        if (spus.size() != ids.size()) {
            log.warn("部分商品不存在，实际查询到{}个，请求的ID数量为{}", spus.size(), ids.size());
            // 可选：return false; 如果要求所有商品都必须存在
        }

        // 检查商品审核状态
        List<PmsSpu> validSpus = spus.stream()
                .filter(spu -> spu.getAuditStatus() == 1)
                .collect(Collectors.toList());

        if (validSpus.isEmpty()) {
            return false;
        }

        // 批量修改商品状态为上架(1)
        validSpus.forEach(spu -> spu.setStatus(SpuStatusEnum.ON_SALE.getValue()));

        return this.updateBatchById(validSpus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUnpublishSpu(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 先批量查询商品是否存在
        List<PmsSpu> spus = this.listByIds(ids);
        if (spus.size() != ids.size()) {
            log.warn("部分商品不存在，实际查询到{}个，请求的ID数量为{}", spus.size(), ids.size());
            // 可选：return false; 如果要求所有商品都必须存在
        }

        if (spus.isEmpty()) {
            return false;
        }

        // 可选：过滤出已上架的商品
        // List<PmsSpu> validSpus = spus.stream()
        //         .filter(spu -> spu.getStatus() == SpuStatusEnum.ON_SALE.getValue())
        //         .collect(Collectors.toList());
        // 
        // if (validSpus.isEmpty()) {
        //     return false;
        // }

        // 批量修改商品状态为下架(0)
        spus.forEach(spu -> spu.setStatus(SpuStatusEnum.OFF_SALE.getValue()));

        return this.updateBatchById(spus);
    }

    /**
     * 保存SKU，需要替换提交表单中的临时规格ID
     *
     * @param spuId           商品ID
     * @param skuList         SKU列表
     * @param specTempIdIdMap 临时规格ID和持久化数据库得到的规格ID的映射
     * @param supplierId      供应商ID
     * @return 是否成功
     */
    private boolean saveSku(Long spuId, List<PmsSku> skuList, Map<String, Long> specTempIdIdMap, Long supplierId) {
        // 判断是否为供应商后台
        boolean isPlatform = SystemTypeEnum.PLATFORM.getValue().equals(SecurityUtils.getSystemType());

        // 删除SKU
        List<Long> formSkuIds = skuList.stream().map(PmsSku::getId).toList();

        List<Long> dbSkuIds = skuService.list(new LambdaQueryWrapper<PmsSku>().eq(PmsSku::getSpuId, spuId)
                        .select(PmsSku::getId)).stream().map(PmsSku::getId)
                .toList();

        List<Long> removeSkuIds = dbSkuIds.stream().filter(dbSkuId -> !formSkuIds.contains(dbSkuId)).collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(removeSkuIds)) {
            skuService.removeByIds(removeSkuIds);
        }

        // 新增/修改SKU
        List<PmsSku> pmsSkuList = skuList.stream().map(sku -> {
            // 临时规格ID转换
            String specIds = Arrays.stream(sku.getSpecIds().split("\\|"))
                    .map(specId -> specId.startsWith(ProductConstants.SPEC_TEMP_ID_PREFIX) ? specTempIdIdMap.get(specId) + "" : specId)
                    .collect(Collectors.joining("_"));
            sku.setSpecIds(specIds);
            sku.setSpuId(spuId);

            // 保持原有的供应商ID，新增时从外层传入
            if (sku.getId() != null) {
                PmsSku existingSku = skuService.getById(sku.getId());
                if (existingSku != null) {
                    sku.setSupplierId(existingSku.getSupplierId());
                }
            } else {
                sku.setSupplierId(supplierId);
            }

            return sku;
        }).collect(Collectors.toList());
        
        // 批量保存SKU
        boolean result = skuService.saveOrUpdateBatch(pmsSkuList);
        
        // 供应商后台提交后：统一建议商品类型为普通商品
        if (!isPlatform && result) {
            List<PmsSkuVO> skuVOList = skuConverter.entity2VOList(pmsSkuList);
            SpuCalculateResultVO pricingResult = calculateSpuPricingInfo(spuId, skuVOList, null);
            PmsSpu spu = this.getById(spuId);
            if (spu != null) {
                spu.setSpuType(pricingResult.getSuggestedSpuType());
                this.updateById(spu);
                log.info("商品ID:{} 自动设置商品类型为:{}, SKU数量:{}",
                        spuId, pricingResult.getSpuTypeDesc(), pmsSkuList.size());
            }
        }

        if (result) {
            validatePriceOrder(pmsSkuList);
            checkPriceAnomalyAlert(spuId, pmsSkuList);
        }

        return result;
    }

    /**
     * 价格异常预警：零售毛利率低于阈值时通知
     */
    private void checkPriceAnomalyAlert(Long spuId, List<PmsSku> skuList) {
        PmsSpu spu = this.getById(spuId);
        if (spu == null || !Boolean.TRUE.equals(marginAlertEnabled) || anomalyRetailMarginMin == null) {
            return;
        }

        List<Map<String, Object>> alertSkus = new ArrayList<>();
        for (PmsSku sku : skuList) {
            BigDecimal supply = sku.getSupplyPrice();
            BigDecimal retail = sku.getRetailPrice();
            if (supply == null || retail == null || retail.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            double retailMargin = retail.subtract(supply)
                    .divide(retail, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
            if (retailMargin >= anomalyRetailMarginMin) {
                continue;
            }
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("skuName", sku.getName());
            alert.put("skuId", sku.getId());
            alert.put("supply", supply);
            alert.put("retail", retail);
            alert.put("retailMargin", retailMargin);
            alert.put("retailBelow", true);
            alertSkus.add(alert);
        }
        if (alertSkus.isEmpty()) {
            return;
        }
        sendMarginAlertNotify(spu, alertSkus, "PRICE_ANOMALY_ALERT");
    }

    /**
     * 构建告警通知的通用 SPU 级参数（spuName、spuId 等）
     */
    private Map<String, Object> buildAlertBaseParams(PmsSpu spu) {
        Map<String, Object> params = new HashMap<>();
        params.put("spuName", spu.getName().replace("\n", " ").replace("\r", " "));
        params.put("spuId", spu.getId().toString());
        params.put("spuType", com.deko.mall.common.base.IBaseEnum.getLabelByValue(
                spu.getSpuType(), SpuTypeEnum.class));
        params.put("operator", SecurityUtils.getUsername());
        params.put("time", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return params;
    }

    /**
     * 发送告警 MQ 消息（通用）
     *
     * @param spu          商品
     * @param templateCode 模板编码
     * @param params       已组装好的模板参数（含 skuList、阈值等）
     */
    private void sendAlertMq(PmsSpu spu, String templateCode, Map<String, Object> params) {
        try {
            String businessId = spu.getId() + "-" + templateCode + "-"
                    + System.currentTimeMillis();

            SystemNotifyMessage msg = SystemNotifyMessage.builder()
                    .templateCode(templateCode)
                    .params(params)
                    .businessId(spu.getId().toString())
                    .source(MqBizConstants.MessageSource.PMS_SERVICE)
                    .build();

            messageSendService.sendMessage(MessageSendDTO.builder()
                .exchange(MqBizConstants.SystemNotify.EXCHANGE) .routingKey(MqBizConstants.SystemNotify.ROUTING_KEY) .payload(msg) .businessId(businessId) .messageType(MqBizConstants.MessageType.SYSTEM_NOTIFY) .source(MqBizConstants.MessageSource.PMS_SERVICE)
                .build());
        } catch (Exception e) {
            log.error("发送告警通知失败: spuId={}, templateCode={}, error={}",
                    spu.getId(), templateCode, e.getMessage(), e);
        }
    }

    /**
     * 通过 MQ 发送毛利率告警企微通知（零售毛利率）
     */
    private void sendMarginAlertNotify(PmsSpu spu, List<Map<String, Object>> alertSkus, String templateCode) {
        List<Map<String, Object>> skuList = new ArrayList<>();
        for (Map<String, Object> sku : alertSkus) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", sku.get("skuName"));
            item.put("id", sku.get("skuId"));
            item.put("retailMargin", String.format("%.1f", sku.get("retailMargin")));
            item.put("retailBelow", sku.get("retailBelow"));
            item.put("priceInfo", "供货" + formatPrice((BigDecimal) sku.get("supply"))
                    + " / 零售" + formatPrice((BigDecimal) sku.get("retail")));
            skuList.add(item);
        }

        Map<String, Object> params = buildAlertBaseParams(spu);
        params.put("skus", skuList);
        params.put("retailThreshold", String.format("%.0f", anomalyRetailMarginMin));
        sendAlertMq(spu, templateCode, params);
    }

    private static String formatPrice(BigDecimal price) {
        return String.format("%.2f", price);
    }

    /**
     * 保存商品属性
     *
     * @param spuId    商品ID
     * @param attrList 商品属性集合
     */
    private boolean saveSpuAttrs(Long spuId, List<PmsSpuAttributeForm> attrList) {

        // 1. 【删除】此次提交移除的商品规格

        // 1.1 此次提交保留的商品属性ID
        List<Long> retainAttrIds = attrList.stream()
                .filter(item -> item.getId() != null)
                .map(item -> Convert.toLong(item.getId()))
                .toList();
        // 1.2 获取原商品属性ID集合
        List<Long> originAttrIds = spuAttributeService.list(new LambdaQueryWrapper<PmsSpuAttribute>()
                        .eq(PmsSpuAttribute::getSpuId, spuId).eq(PmsSpuAttribute::getType, AttributeTypeEnum.ATTR.getValue())
                        .select(PmsSpuAttribute::getId))
                .stream()
                .map(PmsSpuAttribute::getId)
                .toList();
        // 1.3 需要删除的商品属性：原商品属性-此次提交保留的属性
        List<Long> removeAttrValIds = originAttrIds.stream()
                .filter(id -> !retainAttrIds.contains(id))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(removeAttrValIds)) {
            spuAttributeService.removeByIds(removeAttrValIds);
        }

        // 新增或修改商品属性
        List<PmsSpuAttribute> entities = attrList.stream().map(item -> {
            PmsSpuAttribute entity = spuAttributeConverter.form2Entity(item);
            entity.setId(Convert.toLong(item.getId()));
            entity.setSpuId(spuId);
            entity.setType(AttributeTypeEnum.ATTR.getValue());
            return entity;
        }).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(entities)) {
            return spuAttributeService.saveOrUpdateBatch(entities);
        }
        return true;
    }

    /**
     * 保存商品规格
     * <p>
     * 新增的规格需要返回临时ID和持久化到数据库的ID的映射关系，替换SKU中的规格ID集合
     *
     * @param spuId    商品ID
     * @param specList 规格列表
     * @return Map: key-临时ID；value-持久化返回ID
     */
    private Map<String, Long> saveSpuSpecs(Long spuId, List<PmsSpuAttributeForm> specList) {

        // 1. 【删除】此次提交移除的商品规格
        // 1.1 此次提交保留的规格
        List<Long> retainSpuSpecIds = specList.stream()
                .filter(item -> !item.getId().startsWith(ProductConstants.SPEC_TEMP_ID_PREFIX))
                .map(item -> Convert.toLong(item.getId()))
                .toList();

        // 1.2 原商品规格
        List<Long> originSpuSpecIds = spuAttributeService.list(new LambdaQueryWrapper<PmsSpuAttribute>()
                        .eq(PmsSpuAttribute::getSpuId, spuId)
                        .eq(PmsSpuAttribute::getType, AttributeTypeEnum.SPEC.getValue())
                        .select(PmsSpuAttribute::getId))
                .stream().map(PmsSpuAttribute::getId)
                .toList();

        // 1.3 需要删除的商品规格：原商品规格-此次提交保留的规格
        List<Long> removeSpuSpecIds = originSpuSpecIds.stream().filter(id -> !retainSpuSpecIds.contains(id))
                .collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(removeSpuSpecIds)) {
            // 删除商品的规格
            spuAttributeService.removeByIds(removeSpuSpecIds);
        }

        // 2. 【新增】此次提交的新加的商品规格
        // 临时规格ID和持久化数据库得到的规格ID的映射，用于替换SKU临时的规格ID
        Map<String, Long> tempWithNewSpecIdMap = new HashMap<>();
        List<PmsSpuAttributeForm> newSpecList = specList.stream()
                .filter(item -> item.getId().startsWith(ProductConstants.SPEC_TEMP_ID_PREFIX))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(newSpecList)) {
            newSpecList.forEach(item -> {
                PmsSpuAttribute entity = spuAttributeConverter.form2Entity(item);
                entity.setSpuId(spuId);
                entity.setType(AttributeTypeEnum.SPEC.getValue());
                spuAttributeService.save(entity);
                tempWithNewSpecIdMap.put(item.getId(), entity.getId());
            });
        }

        //  3. 【修改】此次提交的需要修改的商品规格
        List<PmsSpuAttribute> pmsSpuAttributeList = specList.stream()
                .filter(item -> !item.getId().startsWith(ProductConstants.SPEC_TEMP_ID_PREFIX))
                .map(item -> {
                    PmsSpuAttribute entity = spuAttributeConverter.form2Entity(item);
                    entity.setId(Convert.toLong(item.getId()));
                    entity.setSpuId(spuId);
                    entity.setType(AttributeTypeEnum.SPEC.getValue());
                    return entity;
                }).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(pmsSpuAttributeList)) {
            spuAttributeService.updateBatchById(pmsSpuAttributeList);
        }
        return tempWithNewSpecIdMap;
    }

    /**
     * 获取商品销量映射（展示销量 = 真实销量）
     *
     * @param spuIds SPU ID列表
     * @return SPU销量映射，key为SPU ID，value为展示销量
     */
    @Override
    public Map<Long, Integer> getSpuSalesMap(List<Long> spuIds) {
        if (CollectionUtil.isEmpty(spuIds)) {
            return Collections.emptyMap();
        }

        // 从数据库获取 SPU 的真实展示销量
        List<PmsSpu> spuList = this.listByIds(spuIds);

        Map<Long, Integer> salesMap = new HashMap<>();
        for (PmsSpu spu : spuList) {
            salesMap.put(spu.getId(), spu.getSales() != null ? spu.getSales() : 0);
        }

        return salesMap;
    }

    /**
     * 获取SPU的默认SKU ID
     * 用于获取默认规格的价格和库存信息
     *
     * @param spuId SPU ID
     * @return 默认SKU ID
     */
    @Override
    public Long getDefaultSkuId(Long spuId) {
        if (spuId == null) {
            return null;
        }

        // 查询指定SPU下的默认SKU
        // 查询未删除的SKU并按照零售价格排序
        LambdaQueryWrapper<PmsSku> queryWrapper = new LambdaQueryWrapper<PmsSku>()
                .eq(PmsSku::getSpuId, spuId)
                // 注意：deleted字段已经通过@TableLogic注解自动处理，查询时会自动加上deleted=0条件
                .orderByAsc(PmsSku::getRetailPrice)  // 按照零售价格升序
                .last("LIMIT 1");  // 取第一个

        PmsSku defaultSku = skuService.getOne(queryWrapper);
        return defaultSku != null ? defaultSku.getId() : null;
    }

    /**
     * 获取SKU的价格和库存信息
     * 批量获取多个SKU的价格、库存和上架状态等动态数据
     *
     * @param skuIds SKU ID列表
     * @return SKU信息映射，key为SKU ID，value为包含价格、库存等信息的Map
     */
    @Override
    public Map<Long, Map<String, Object>> getSkuInfoMap(List<Long> skuIds) {
        if (CollectionUtil.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }

        Map<Long, Map<String, Object>> resultMap = new HashMap<>();

        try {
            // 根据SKU ID列表获取SKU信息
            List<PmsSku> skuList = skuService.listByIds(skuIds);

            for (PmsSku sku : skuList) {
                Map<String, Object> skuInfo = new HashMap<>();

                // 添加价格信息
                skuInfo.put("price", sku.getRetailPrice());

                // 添加库存信息
                skuInfo.put("stock", sku.getStock());

                // 查询SPU的上架状态
                PmsSpu spu = this.getById(sku.getSpuId());
                Integer publishStatus = spu != null ? spu.getStatus() : 0;
                skuInfo.put("publishStatus", publishStatus);

                // 将SKU信息添加到结果映射中
                resultMap.put(sku.getId(), skuInfo);
            }

            log.info("批量获取SKU信息成功，共{}项", skuList.size());
        } catch (Exception e) {
            log.error("获取SKU信息失败", e);
        }

        return resultMap;
    }

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
    @Override
    public Map<String, SpuDynamicDataDTO> getSpuDynamicDataMap(List<Long> spuIds) {
        if (CollectionUtil.isEmpty(spuIds)) {
            return Collections.emptyMap();
        }

        Map<String, SpuDynamicDataDTO> resultMap = new HashMap<>();

        try {
            // 1. 获取所有SPU实体
            List<PmsSpu> spuList = this.listByIds(spuIds);
            if (spuList.isEmpty()) {
                return resultMap;
            }
            Map<Long, PmsSpu> spuMap = spuList.stream()
                    .collect(Collectors.toMap(PmsSpu::getId, spu -> spu));

            // 2. 为每个SPU获取所有SKU
            Map<Long, List<PmsSku>> spuSkuMap = new HashMap<>(); // spuId -> 全部SKU
            for (Long spuId : spuIds) {
                // 使用MyBatis-Plus条件查询获取指定SPU的所有SKU
                List<PmsSku> skuList = skuService.lambdaQuery()
                        .eq(PmsSku::getSpuId, spuId)
                        .list();
                if (!skuList.isEmpty()) {
                    spuSkuMap.put(spuId, skuList);
                }
            }

            // 3. 组装结果，按要求求最低价格和总库存
            for (Long spuId : spuIds) {
                SpuDynamicDataDTO data = new SpuDynamicDataDTO();
                data.setSpuId(spuId);

                // 默认值
                data.setMinPrice(0L);
                data.setTotalStock(0);
                data.setPublishStatus(0);
                data.setSales(0);

                // 设置SPU发布状态和销量（直接从SPU表获取）
                PmsSpu spu = spuMap.get(spuId);
                if (spu != null) {
                    data.setPublishStatus(spu.getStatus());
                    data.setSales(spu.getSales() != null ? spu.getSales() : 0);
                    data.setRealSales(spu.getRealSales() != null ? spu.getRealSales() : 0);
                    data.setVirtualSales(spu.getVirtualSales() != null ? spu.getVirtualSales() : 0);
                }

                // 处理SKU数据（最低价格和总库存）
                List<PmsSku> skuList = spuSkuMap.get(spuId);
                if (skuList != null && !skuList.isEmpty()) {
                    // 1. 找到最低零售价
                    BigDecimal minPriceBigDecimal = skuList.stream()
                            .filter(sku -> sku.getRetailPrice() != null)
                            .map(PmsSku::getRetailPrice)
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);

                    data.setMinPrice(minPriceBigDecimal.longValue());

                    // 2. 累加所有SKU的库存
                    int totalStock = skuList.stream()
                            .filter(sku -> sku.getStock() != null)
                            .mapToInt(PmsSku::getStock)
                            .sum();
                    data.setTotalStock(totalStock);
                }

                // 添加到结果集
                resultMap.put("s" + spuId, data);
            }

            log.info("批量获取SPU动态数据成功，共{}项", spuIds.size());
        } catch (Exception e) {
            log.error("获取SPU动态数据失败", e);
        }

        return resultMap;
    }

    @Override
    public void calculateMargins(PmsSkuVO skuVO) {
        BigDecimal retailPrice = skuVO.getRetailPrice();
        BigDecimal supplyPrice = skuVO.getSupplyPrice();
        if (retailPrice != null && supplyPrice != null && retailPrice.compareTo(BigDecimal.ZERO) > 0) {
            double retailMargin = retailPrice.subtract(supplyPrice)
                    .divide(retailPrice, 4, RoundingMode.HALF_UP).doubleValue() * 100;
            skuVO.setRetailPriceMargin(Math.round(retailMargin * 100.0) / 100.0);
        }
    }

    @Override
    public void calculateSkuPrice(PmsSkuVO skuVO) {
        calculateMargins(skuVO);
    }

    @Override
    public void calculateSkuMargin(PmsSkuVO skuVO) {
        calculateMargins(skuVO);
    }

    @Override
    public SpuCalculateResultVO calculateSpuPricingInfo(Long spuId, List<PmsSkuVO> skuList, String manualSpuType) {
        if (CollectionUtil.isEmpty(skuList)) {
            throw new IllegalArgumentException("SKU列表不能为空");
        }
        SpuCalculateResultVO result = new SpuCalculateResultVO();
        for (PmsSkuVO sku : skuList) {
            calculateMargins(sku);
        }
        String spuType = manualSpuType != null ? manualSpuType : SpuTypeEnum.NORMAL.getValue();
        result.setSuggestedSpuType(spuType);
        result.setSpuTypeDesc(IBaseEnum.getLabelByValue(spuType, SpuTypeEnum.class));
        result.setSkuList(skuList);
        log.info("SPU:{} 建议类型={}", spuId, result.getSpuTypeDesc());
        return result;
    }

    private void validatePriceOrder(java.util.List<PmsSku> skuList) {
        for (int i = 0; i < skuList.size(); i++) {
            PmsSku sku = skuList.get(i);
            BigDecimal supply = sku.getSupplyPrice();
            BigDecimal retail = sku.getRetailPrice();
            if (supply == null || retail == null) {
                continue;
            }
            if (supply.compareTo(retail) > 0) {
                throw new IllegalArgumentException(
                        String.format("SKU[%d] 供货价(%.2f)不能大于零售价(%.2f)", i + 1, supply, retail));
            }
        }
    }

    /**
     * 根据供应商ID查询对应店铺ID（供应商→店铺 1:1 映射）
     */
    private Long findShopIdBySupplierId(Long supplierId) {
        if (supplierId == null) {
            return null;
        }
        PmsShop pmsShop = pmsShopMapper.selectOne(new LambdaQueryWrapper<PmsShop>()
                .eq(PmsShop::getOwnerType, "SUPPLIER")
                .eq(PmsShop::getOwnerId, supplierId)
                .last("LIMIT 1"));
        return pmsShop != null ? pmsShop.getId() : null;
    }

}
