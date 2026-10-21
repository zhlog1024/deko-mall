package com.deko.mall.ums.mapper;

import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * sys_region 只读查询（跨模块读同一数据库）
 */
@Mapper
public interface SysRegionMapper {

    @Select("SELECT id, name FROM sys_region WHERE level = 1 AND deleted = 0 AND hidden = 0 ORDER BY sort")
    List<RegionSimple> findProvinces();

    @Select("SELECT id, name FROM sys_region WHERE parent_id = #{parentId} AND deleted = 0 AND hidden = 0 ORDER BY sort")
    List<RegionSimple> findChildrenByParentId(@Param("parentId") Long parentId);

    @Data
    class RegionSimple {
        private Long id;
        private String name;
    }
}
