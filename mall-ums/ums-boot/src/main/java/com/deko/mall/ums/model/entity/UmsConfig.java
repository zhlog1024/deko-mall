package com.deko.mall.ums.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 贡献计算配置实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ums_config")
public class UmsConfig extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String model;
    private String biz;
    private BigDecimal configValue;

    /**
     * 配置描述
     */
    private String des;

    /**
     * 逻辑删除标志（0-未删除，1-已删除）
     */
    private Integer deleted;
}