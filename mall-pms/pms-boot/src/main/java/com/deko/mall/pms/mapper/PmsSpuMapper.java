package com.deko.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.entity.PmsSpu;
import com.deko.mall.pms.model.query.SpuPageQuery;
import com.deko.mall.pms.model.vo.PmsSpuPageVO;
import com.deko.mall.pms.model.vo.SpuPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PmsSpuMapper extends BaseMapper<PmsSpu> {

    /**
     * Admin-商品分页列表
     *
     * @param page        分页参数
     * @param queryParams 查询参数
     * @return 商品分页列表
     */
    List<PmsSpuPageVO> listPagedSpu(Page<PmsSpuPageVO> page, @Param("queryParams") SpuPageQuery queryParams);

    /**
     * APP-商品分页列表
     *
     * @param page        分页参数
     * @param queryParams 查询参数
     * @return 商品分页列表
     */
    List<SpuPageVO> listPagedSpuForApp(Page<SpuPageVO> page, @Param("queryParams") SpuPageQuery queryParams);


    /**
     * 增加商品真实销量（同时更新 real_sales 和展示销量 sales）
     *
     * @param spuId    SPU ID
     * @param quantity 增加的数量
     * @return 影响行数
     */
    int incrementSales(@Param("spuId") Long spuId, @Param("quantity") Integer quantity);

    /**
     * 批量查询SPU信息（包含SKU最低价）
     *
     * @param spuIds SPU ID列表
     * @return SPU信息列表，包含price和originPrice字段（从SKU取最低价）
     */
    List<Map<String, Object>> listSpuWithMinPrice(@Param("spuIds") List<Long> spuIds);


    /**
     * 查询搜索结果中的去重品牌ID列表
     */
    List<Long> selectDistinctBrandIds(@Param("keywords") String keywords, @Param("categoryId") Long categoryId);

}
