package com.deko.mall.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.common.web.model.Option;
import com.deko.mall.system.model.entity.SysMenu;
import com.deko.mall.system.model.form.MenuForm;
import com.deko.mall.system.model.query.MenuQuery;
import com.deko.mall.system.model.vo.MenuVO;
import com.deko.mall.system.model.vo.RouteVO;

import java.util.List;

/**
 * 菜单业务接口
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取菜单表格列表
     */
    List<MenuVO> listMenus(MenuQuery queryParams);


    /**
     * 获取菜单下拉列表
     */
    List<Option> listMenuOptions(String systemType);

    /**
     * 新增菜单
     */
    boolean saveMenu(MenuForm menu);

    /**
     * 获取路由列表
     */
    List<RouteVO> listRoutes();

    /**
     * 修改菜单显示状态
     *
     * @param menuId  菜单ID
     * @param visible 是否显示(1->显示；2->隐藏)
     */
    boolean updateMenuVisible(Long menuId, Integer visible);

    /**
     * 获取菜单表单数据
     *
     * @param id 菜单ID
     */
    MenuForm getMenuForm(Long id);

    /**
     * 删除菜单
     */
    boolean deleteMenu(Long id);
}
