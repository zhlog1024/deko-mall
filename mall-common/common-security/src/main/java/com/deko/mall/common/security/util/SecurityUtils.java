package com.deko.mall.common.security.util;

import cn.hutool.core.convert.Convert;
import com.deko.mall.common.constant.SystemConstants;
import com.deko.mall.common.enums.SystemTypeEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 工具类
 */
public class SecurityUtils {

    public static Long getUserId() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return Convert.toLong(tokenAttributes.get("userId"));
        }
        return null;
    }

    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    public static Map<String, Object> getTokenAttributes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getTokenAttributes();
        }
        return null;
    }


    /**
     * 获取用户角色集合
     */
    public static Set<String> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                    .stream()
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
        }
        return null;
    }

    /**
     * 获取部门ID
     */
    public static Long getDeptId() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return Convert.toLong(tokenAttributes.get("deptId"));
        }
        return null;
    }

    public static boolean isRoot() {
        Set<String> roles = getRoles();
        return roles != null && roles.contains(SystemConstants.ROOT_ROLE_CODE);
    }

    public static String getJti() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return String.valueOf(tokenAttributes.get("jti"));
        }
        return null;
    }


    public static Long getExp() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return Convert.toLong(tokenAttributes.get("exp"));
        }
        return null;
    }

    /**
     * 获取数据权限范围
     */
    public static Integer getDataScope() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return Convert.toInt(tokenAttributes.get("dataScope"));
        }
        return null;
    }

    /**
     * 获取会员ID
     *
     * @return 会员ID
     */
    public static Long getMemberId() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return Convert.toLong(tokenAttributes.get("memberId"));
        }
        return null;
    }

    /**
     * 获取会员手机号
     *
     * @return 会员手机号
     */
    public static String getMemberPhone() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return Convert.toStr(tokenAttributes.get("memberPhone"));
        }
        return null;
    }


    /**
     * 获取供应商ID
     */
    public static Long getSupplierId() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return Convert.toLong(tokenAttributes.get("supplierId"));
        }
        return null;
    }

    /**
     * 判断当前用户是否为供应商用户
     */
    public static boolean isSupplier() {
        return getSupplierId() != null && SystemTypeEnum.SUPPLIER.getValue().equals(getSystemType());
    }

    /**
     * 获取用户系统类型
     */
    public static String getSystemType() {
        Map<String, Object> tokenAttributes = getTokenAttributes();
        if (tokenAttributes != null) {
            return String.valueOf(tokenAttributes.get("systemType"));
        }
        return null;
    }

}
