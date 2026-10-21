package com.deko.mall.pms.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类VO（用于Feign客户端传输）
 */
@Data
public class CategoryVO {

    private Long id;

    private String name;

    private Long parentId;

    private Integer level;

    private String iconUrl;

    private Integer sort;

    private Integer visible;

    private List<CategoryVO> children = new ArrayList<>();
}

