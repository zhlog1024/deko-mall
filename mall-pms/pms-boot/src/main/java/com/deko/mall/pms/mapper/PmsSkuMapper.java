package com.deko.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.dto.SkuInfoDTO;
import com.deko.mall.pms.model.entity.PmsSku;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.SkuInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmsSkuMapper extends BaseMapper<PmsSku> {

    /**
     * 获取商品库存单元信息
     */
    SkuInfoDTO getSkuInfo(Long skuId);

    /**
     * 查询SKU信息列表及关联的SPU信息
     *
     * @param skuIds SKU ID列表
     * @return SKU信息列表(包含SPU信息)
     */
    List<SkuInfoDTO> listSkuInfosWithSpu(@Param("skuIds") List<Long> skuIds);

    List<SkuInfoVO> listPagedSku(Page<SkuInfoVO> page, @Param("queryParams") SpuPageQuery queryParams);


    /**
     * 按名称 / SKU 编码模糊搜索 SKU ID（库存页关键字筛选用）
     *
     * @param keyword 关键字
     * @param limit   最多返回条数
     * @return SKU ID 列表
     */
    List<Long> searchSkuIdsByNameOrSn(@Param("keyword") String keyword, @Param("limit") int limit);
}
