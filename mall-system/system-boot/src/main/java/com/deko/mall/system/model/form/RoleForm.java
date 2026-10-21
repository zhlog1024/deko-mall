package com.deko.mall.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "角色表单对象")
@Data
public class RoleForm {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Schema(description = "角色编码")
    @NotBlank(message = "角色编码不能为空")
    private String code;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "角色状态(1-正常；0-停用)")
    private Integer status;

    @Schema(description = "数据权限")
    private Integer dataScope;

    @Schema(description = "系统类型(PLATFORM:平台系统;SUPPLIER:供应商系统)")
    private String systemType;

}
