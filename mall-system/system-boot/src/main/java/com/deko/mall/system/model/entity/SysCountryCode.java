package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * 国家电话区号实体
 */
@Data
@TableName("sys_country_code")
public class SysCountryCode extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** ISO 3166-1 alpha-2 国家代码 */
    private String isoCode;

    /** ISO 3166-1 alpha-3 */
    private String isoCode3;

    /** 中文名 */
    private String nameZh;

    /** 英文名 */
    private String nameEn;

    /** 电话区号(如+86) */
    private String phoneCode;

    /** 手机号长度范围(如11或9,10) */
    private String phoneLength;

    /** 手机号校验正则 */
    private String phoneRegex;

    /** 国旗emoji */
    private String flagEmoji;

    /** 所属大洲 */
    private String continent;

    /** 状态:0禁用 1启用 */
    private Integer status;

    /** 排序 */
    private Integer sort;
}
