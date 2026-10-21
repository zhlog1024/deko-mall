package com.deko.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.pms.model.entity.PmsCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PmsCategoryMapper extends BaseMapper<PmsCategory> {

    /**
     * 查询有商品的三级类目
     */
    List<PmsCategory> selectValidCategoriesWithProducts();

}
