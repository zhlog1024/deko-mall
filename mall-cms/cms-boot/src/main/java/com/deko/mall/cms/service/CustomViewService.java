package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.api.dto.CustomViewItemDTO;
import com.deko.mall.cms.model.dto.CustomViewSaveDTO;
import com.deko.mall.cms.model.entity.CustomView;
import com.deko.mall.cms.model.query.CustomViewPageQuery;
import com.deko.mall.cms.model.vo.CustomViewAppVO;
import com.deko.mall.cms.model.vo.CustomViewDetailVO;
import com.deko.mall.cms.model.vo.CustomViewVO;

import java.util.List;

/**
 * 自定义页面服务
 */
public interface CustomViewService extends IService<CustomView> {

    /**
     * 管理端分页
     */
    IPage<CustomViewVO> pageCustomViews(CustomViewPageQuery query);

    /**
     * 管理端详情（编辑回显,含正文代码）
     */
    CustomViewDetailVO getDetail(Long id);

    /**
     * 新增页面
     */
    boolean saveCustomView(CustomViewSaveDTO dto);

    /**
     * 编辑页面（支持局部字段更新:行内启用开关等）
     */
    boolean updateCustomView(CustomViewSaveDTO dto);

    /**
     * 删除页面（逻辑删除）
     */
    boolean deleteCustomView(Long id);

    /**
     * App 端页面详情（启用且未删;访问量+1）
     */
    CustomViewAppVO getAppView(Long id);

    /**
     * 启用页面列表（feign:装修链接选择/官网）
     */
    List<CustomViewItemDTO> listEnabledFeign();

    /**
      * Web/官网端整页渲染 HTML
     *
     * @param id 页面ID
     * @return 完整 HTML 文档: SEO head + style(css) + body(html) + script(js)
     */
    String getWebPageHtml(Long id);
}
