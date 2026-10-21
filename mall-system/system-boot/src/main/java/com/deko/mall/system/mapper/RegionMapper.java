package com.deko.mall.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.system.model.entity.Region;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 中国行政地区表(Region)表数据库访问层
 */
@Mapper
public interface RegionMapper extends BaseMapper<Region> {

    /**
     * 省市区层级查询
     */
    List<Region> listAllTree(Long provinceId, Long cityId, Long districtId);

}
