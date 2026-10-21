package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频 App 端列表查询（用户端渲染，仅取启用未删；排序语义对齐管理端）
 */
@Schema(description = "视频App列表查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class VideoAppQuery extends BasePageQuery {

    @Schema(description = "关键字(标题模糊)")
    private String keyword;

    @Schema(description = "是否推荐(1是/0否,空=全部)")
    private Integer isRecommended;

    @Schema(description = "是否热门(1是/0否,空=全部)")
    private Integer isHot;

    @Schema(description = "排序字段:sort=自定义排序优先(升序)+创建时间倒序(默认);createTime=创建时间;accessCount=浏览数;duration=时长;DIY orderByType 由装修器映射后进入")
    private String sortField;

    @Schema(description = "排序规则(缺省0):0=降序/1=升序;对 sortField=createTime/accessCount/duration 生效;综合(sortField 空或 sort)固定不受影响")
    private Integer orderByRule;
}
