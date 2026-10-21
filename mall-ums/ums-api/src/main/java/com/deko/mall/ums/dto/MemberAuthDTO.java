package com.deko.mall.ums.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会员信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberAuthDTO {

    /**
     * 会员ID
     */
    private Long id;

    /**
     * 会员名(openId、mobile)
     */
    private String username;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 状态(1:正常；0：禁用)
     */
    private Integer status;

    private String password;
}
