package com.deko.mall.system.mapper;

/**
 * 菜单持久接口层
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.system.model.bo.RouteBO;
import com.deko.mall.system.model.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<RouteBO> listRoutes();
}
