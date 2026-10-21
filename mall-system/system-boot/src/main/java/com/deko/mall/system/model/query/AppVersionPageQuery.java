package com.deko.mall.system.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP版本分页查询对象")
@Data
public class AppVersionPageQuery extends BasePageQuery {

    @Schema(description = "关键字(版本名称/版本号)")
    private String keywords;

    @Schema(description = "平台(android/ios)")
    private String platform;

    @Schema(description = "是否启用(0:否 1:是)")
    private Integer isEnabled;
}
