package com.deko.mall.ums.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 会员传输层对象
 */
@Data
public class MemberRegisterDTO {

    private Integer gender;

    private String nickName;

    private String password;

    private String mobile;

    /** 手机国家区号(如+86) */
    private String countryCode;

    private LocalDate birthday;

    private String avatarUrl;

    private String openid;

    //分享用户ID
    private Long shareUserId;

    private String sessionKey;

    private String city;

    private String country;

    private String language;

    private String province;

    /** 短信验证码（历史字段名 invitationCode，实际校验短信缓存） */
    private String invitationCode;

}
