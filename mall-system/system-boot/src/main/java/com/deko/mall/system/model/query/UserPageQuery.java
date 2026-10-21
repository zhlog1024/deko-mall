package com.deko.mall.system.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询对象
 */
@Schema
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageQuery extends BasePageQuery {

    @Schema(description = "关键字(用户名/昵称/手机号)")
    private String keywords;

    @Schema(description = "用户状态")
    private Integer status;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "系统类型(PLATFORM:平台系统;SUPPLIER:供应商系统)")
    private String systemType;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号码")
    private String mobile;

}
