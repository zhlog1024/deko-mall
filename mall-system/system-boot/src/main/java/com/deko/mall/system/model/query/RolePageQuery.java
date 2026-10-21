package com.deko.mall.system.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageQuery extends BasePageQuery {

    @Schema(description = "关键字(角色名称/角色编码)")
    private String keywords;

    @Schema(description = "系统类型(PLATFORM:平台系统;SUPPLIER:供应商系统)")
    private String systemType;
}
