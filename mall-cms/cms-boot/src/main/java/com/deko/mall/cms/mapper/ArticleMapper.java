package com.deko.mall.cms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.cms.model.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
