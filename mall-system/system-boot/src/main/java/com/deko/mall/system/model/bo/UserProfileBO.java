package com.deko.mall.system.model.bo;

import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * 用户个人中心对象
 */
@Data
public class UserProfileBO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 用户角色名称集合
     */
    private Set<String> roleNames;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 系统类型
     */
    private String systemType;

}
