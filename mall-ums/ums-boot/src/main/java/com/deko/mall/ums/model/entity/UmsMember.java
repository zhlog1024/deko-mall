package com.deko.mall.ums.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UmsMember extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer gender;
    private String nickName;
    private String password;
    private String mobile;
    /** 手机国家区号(如+86) */
    private String countryCode;
    private LocalDate birthday;

    private String avatarUrl;
    private String openid;
    private String sessionKey;
    private String city;
    private String country;
    private String language;
    private String province;
    private Integer status;
    /**
     * 用户可用的支付抵扣金额(元)
     */
    private BigDecimal paymentDiscountAmount;

    @TableLogic(delval = "1", value = "0")
    private Integer deleted;
    @TableField(exist = false)
    private List<UmsAddress> addressList;
    private String tagName;
    /**
     * 角色编码，默认 normal。
     */
    private String roleCode;
    /**
     * 用户展示类型（非数据库字段）
     */
    @TableField(exist = false)
    private String memberType;
}
