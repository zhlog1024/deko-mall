package com.deko.mall.pms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 品牌DTO（用于Feign客户端传输）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PmsBrandDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌ID
     */
    private Long id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 品牌Logo URL
     */
    private String logoUrl;

    /**
     * 排序
     */
    private Integer sort;
}

