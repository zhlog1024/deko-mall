package com.deko.mall.ums.model.form;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员查询参数。
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员查询参数")
@Data
public class UmsMemberQuery extends BasePageQuery {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员昵称")
    private String nickName;

    @Schema(description = "用户手机号码")
    private String mobile;

    @Schema(description = "会员状态：1-正常，0-禁用")
    private Integer status;
}
