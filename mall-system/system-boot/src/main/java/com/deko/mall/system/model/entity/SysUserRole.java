package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户和角色关联表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserRole {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 系统类型
     */
    private String systemType;
}