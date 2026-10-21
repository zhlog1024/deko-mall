package com.deko.mall.system.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息记录分页查询对象
 */
@Schema(description = "消息记录分页查询对象")
@Data
public class SysMsgRecordQuery extends BasePageQuery {

    @Schema(description = "接收者")
    private String receiver;

    @Schema(description = "发送状态(1-成功, 0-失败)")
    private Integer status;

}
