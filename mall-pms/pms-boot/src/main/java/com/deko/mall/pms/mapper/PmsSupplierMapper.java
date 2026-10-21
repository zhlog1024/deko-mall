package com.deko.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.entity.PmsSupplier;
import com.deko.mall.pms.model.query.SupplierPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 供应商Mapper接口
 */
@Mapper
public interface PmsSupplierMapper extends BaseMapper<PmsSupplier> {

    /**
     * 获取供应商分页列表
     *
     * @param page        分页参数
     * @param queryParams 查询参数
     * @return 供应商分页列表
     */
    Page<PmsSupplier> getSupplierPages(Page<PmsSupplier> page, @Param("queryParams") SupplierPageQuery queryParams);
}
