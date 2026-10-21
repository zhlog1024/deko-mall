package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品问答 App 端列表查询（用户端渲染，仅取启用未删）
 */
@Schema(description = "商品问答App列表查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class AskAppQuery extends BasePageQuery {

    @Schema(description = "商品SPU ID(空=全部启用问答)")
    private Long spuId;

    @Schema(description = "关键字(问题标题模糊)")
    private String keyword;

    @Schema(description = "是否已回复(1是/0否,空=全部)")
    private Integer isReply;

    @Schema(description = "排序类型(与DIY编辑器选项一致,缺省0):0=综合(固定 sort asc+创建时间倒序)/1=最新(按创建时间,方向由orderByRule决定)")
    private Integer orderByType;

    @Schema(description = "排序规则(缺省0):0=降序/1=升序;仅 orderByType=1(最新)生效,综合固定不受影响")
    private Integer orderByRule;
}
