package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.api.dto.ArticleAutoQuery;
import com.deko.mall.cms.api.dto.ArticleItemDTO;
import com.deko.mall.cms.model.dto.ArticleSaveDTO;
import com.deko.mall.cms.model.entity.Article;
import com.deko.mall.cms.model.query.ArticleAppQuery;
import com.deko.mall.cms.model.query.ArticlePageQuery;
import com.deko.mall.cms.model.vo.ArticleAppDetailVO;
import com.deko.mall.cms.model.vo.ArticleAppItemVO;
import com.deko.mall.cms.model.vo.ArticleDetailVO;
import com.deko.mall.cms.model.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务
 */
public interface ArticleService extends IService<Article> {

    /**
     * 管理端分页列表
     */
    IPage<ArticleVO> pageArticles(ArticlePageQuery query);

    /**
     * 管理端详情（编辑回显，含正文）
     */
    ArticleDetailVO getDetail(Long id);

    /**
     * 新增文章
     */
    boolean createArticle(ArticleSaveDTO dto);

    /**
     * 编辑文章
     */
    boolean updateArticle(ArticleSaveDTO dto);

    /**
     * 删除文章（逻辑删除）
     */
    boolean deleteArticle(Long id);

    /**
     * App 端分页列表（启用 + 分类含子级 + 排序 + 封面过滤）
     */
    IPage<ArticleAppItemVO> appList(ArticleAppQuery query);

    /**
     * App 端详情（访问量+1）
     */
    ArticleAppDetailVO appDetail(Long id);

    /**
     * Feign：指定文章数据（启用未删，按 ids）
     */
    List<ArticleItemDTO> listByIdsFeign(List<Long> ids);

    /**
     * Feign：自动文章数据（启用未删，分类含子级 + 排序 + 数量）
     */
    List<ArticleItemDTO> autoListFeign(ArticleAutoQuery query);
}
