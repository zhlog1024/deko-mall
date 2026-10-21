package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品问答分页查询（Feign，供装修器选问答）
 */
@Data
@Schema(description = "商品问答分页查询")
public class AskFeignPageQuery implements Serializable {

    @Schema(description = "页码", example = "1")
    private int pageNum = 1;

    @Schema(description = "每页记录数", example = "10")
    private int pageSize = 10;

    @Schema(description = "关键字(问题标题模糊)")
    private String keyword;

    @Schema(description = "商品SPU ID(空=全部)")
    private Long spuId;

    @Schema(description = "是否已回复(1是/0否,空=全部)")
    private Integer isReply;

    @Schema(description = "是否启用(1是/0否,空=默认1)")
    private Integer isEnable;

    @Schema(description = "排序类型(与DIY编辑器选项一致):0=综合(默认,固定 sort asc+创建时间倒序)/1=最新(按创建时间,方向由orderByRule决定)")
    private Integer orderByType;

    @Schema(description = "排序规则:0=降序(默认)/1=升序;仅 orderByType=1(最新)生效,综合固定不受影响")
    private Integer orderByRule;
}
