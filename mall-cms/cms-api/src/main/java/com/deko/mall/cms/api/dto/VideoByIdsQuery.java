package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 视频指定数据查询（Feign，装修器"指定视频"回显）
 */
@Data
@Schema(description = "视频指定数据查询")
public class VideoByIdsQuery implements Serializable {

    @Schema(description = "视频ID列表")
    private List<Long> ids;
}
