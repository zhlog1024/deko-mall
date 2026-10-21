package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.cms.api.dto.CustomViewItemDTO;
import com.deko.mall.cms.mapper.CustomViewMapper;
import com.deko.mall.cms.model.dto.CustomViewSaveDTO;
import com.deko.mall.cms.model.entity.CustomView;
import com.deko.mall.cms.model.query.CustomViewPageQuery;
import com.deko.mall.cms.model.vo.CustomViewAppVO;
import com.deko.mall.cms.model.vo.CustomViewDetailVO;
import com.deko.mall.cms.model.vo.CustomViewVO;
import com.deko.mall.cms.service.CustomViewService;
import com.deko.mall.common.web.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义页面服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomViewServiceImpl extends ServiceImpl<CustomViewMapper, CustomView>
        implements CustomViewService {

    @Override
    public IPage<CustomViewVO> pageCustomViews(CustomViewPageQuery query) {
        Page<CustomView> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CustomView> wrapper = buildBaseWrapper();
        if (query.getIsEnable() != null) {
            wrapper.eq(CustomView::getIsEnable, query.getIsEnable());
        }
        if (StringUtils.isNotBlank(query.getKeywords())) {
            wrapper.like(CustomView::getName, query.getKeywords());
        }
        wrapper.orderByDesc(CustomView::getId);

        IPage<CustomView> result = page(page, wrapper);
        List<CustomViewVO> voList = result.getRecords().stream().map(this::toVO).toList();
        Page<CustomViewVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CustomViewDetailVO getDetail(Long id) {
        CustomView customView = requireCustomView(id, false);
        return toDetailVO(customView);
    }

    @Override
    public boolean saveCustomView(CustomViewSaveDTO dto) {
        CustomView customView = new CustomView();
        BeanUtils.copyProperties(dto, customView);
        if (customView.getIsEnable() == null) {
            customView.setIsEnable(1);
        }
        if (customView.getIsHeader() == null) {
            customView.setIsHeader(1);
        }
        if (customView.getIsFooter() == null) {
            customView.setIsFooter(1);
        }
        if (customView.getIsFullScreen() == null) {
            customView.setIsFullScreen(0);
        }
        customView.setAccessCount(0);
        customView.setDeleted(0);
        return save(customView);
    }

    @Override
    public boolean updateCustomView(CustomViewSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("页面ID不能为空");
        }
        requireCustomView(dto.getId(), false);
        CustomView customView = new CustomView();
        BeanUtils.copyProperties(dto, customView);
        return updateById(customView);
    }

    @Override
    public boolean deleteCustomView(Long id) {
        CustomView upd = new CustomView();
        upd.setId(id);
        upd.setDeleted(1);
        return updateById(upd);
    }

    @Override
    public CustomViewAppVO getAppView(Long id) {
        CustomView customView = requireCustomView(id, true);
        // 访问量自增（SQL 原子更新,不触发字段自动填充）
        UpdateWrapper<CustomView> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).setSql("access_count = access_count + 1");
        update(updateWrapper);

        CustomViewAppVO vo = new CustomViewAppVO();
        vo.setId(customView.getId());
        vo.setLogo(customView.getLogo());
        vo.setName(customView.getName());
        vo.setHtmlContent(customView.getHtmlContent());
        vo.setIsHeader(customView.getIsHeader());
        vo.setIsFooter(customView.getIsFooter());
        vo.setIsFullScreen(customView.getIsFullScreen());
        return vo;
    }

    @Override
    public List<CustomViewItemDTO> listEnabledFeign() {
        LambdaQueryWrapper<CustomView> wrapper = buildBaseWrapper();
        wrapper.eq(CustomView::getIsEnable, 1).orderByDesc(CustomView::getId);
        return list(wrapper).stream().map(this::toItemDTO).toList();
    }

    @Override
    public String getWebPageHtml(Long id) {
        CustomView customView = requireCustomView(id, true);
        // 访问量自增（与 app 详情同 SQL 原子口径,不触发字段自动填充）
        UpdateWrapper<CustomView> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).setSql("access_count = access_count + 1");
        update(updateWrapper);
        return buildWebPageHtml(customView);
    }

    /**
     * 组装官网/Web 整页 HTML
     * SEO 字段做 HTML 转义,防标题/描述内容破坏文档结构
     */
    private String buildWebPageHtml(CustomView customView) {
        String title = StringEscapeUtils.escapeHtml4(
                StringUtils.defaultString(StringUtils.defaultIfBlank(customView.getSeoTitle(), customView.getName())));
        String keywords = StringEscapeUtils.escapeHtml4(StringUtils.defaultString(customView.getSeoKeywords()));
        String desc = StringEscapeUtils.escapeHtml4(StringUtils.defaultString(customView.getSeoDesc()));
        String css = StringUtils.defaultString(customView.getCssContent());
        String html = StringUtils.defaultString(customView.getHtmlContent());
        String js = StringUtils.defaultString(customView.getJsContent());
        return "<!DOCTYPE html>\n"
                + "<html lang=\"zh-CN\">\n"
                + "<head>\n"
                + "<meta charset=\"utf-8\" />\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
                + "<title>" + title + "</title>\n"
                + "<meta name=\"keywords\" content=\"" + keywords + "\" />\n"
                + "<meta name=\"description\" content=\"" + desc + "\" />\n"
                + "<style>\n" + css + "\n</style>\n"
                + "</head>\n"
                + "<body>\n" + html + "\n"
                + "<script>\n" + js + "\n</script>\n"
                + "</body>\n"
                + "</html>\n";
    }

    // 私有工具

    private LambdaQueryWrapper<CustomView> buildBaseWrapper() {
        LambdaQueryWrapper<CustomView> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomView::getDeleted, 0);
        return wrapper;
    }

    private CustomView requireCustomView(Long id, boolean enabledOnly) {
        CustomView customView = getById(id);
        if (customView == null || customView.getDeleted() != null && customView.getDeleted() == 1) {
            throw new BizException("自定义页面不存在");
        }
        if (enabledOnly && (customView.getIsEnable() == null || customView.getIsEnable() != 1)) {
            throw new BizException("自定义页面已下架");
        }
        return customView;
    }

    private CustomViewVO toVO(CustomView customView) {
        CustomViewVO vo = new CustomViewVO();
        BeanUtils.copyProperties(customView, vo);
        return vo;
    }

    private CustomViewDetailVO toDetailVO(CustomView customView) {
        CustomViewDetailVO vo = new CustomViewDetailVO();
        BeanUtils.copyProperties(customView, vo);
        return vo;
    }

    private CustomViewItemDTO toItemDTO(CustomView customView) {
        CustomViewItemDTO dto = new CustomViewItemDTO();
        dto.setId(customView.getId());
        dto.setName(customView.getName());
        dto.setLogo(customView.getLogo());
        return dto;
    }
}
