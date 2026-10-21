package com.deko.mall.cms.model.query;

import com.deko.mall.common.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频分页查询（管理端）
 */
@Schema(description = "视频分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class VideoPageQuery extends BasePageQuery {

    @Schema(description = "关键字(标题模糊)")
    private String keyword;

    @Schema(description = "是否推荐(1是/0否,空=全部)")
    private Integer isRecommended;

    @Schema(description = "是否热门(1是/0否,空=全部)")
    private Integer isHot;

    @Schema(description = "是否启用(1是/0否,空=全部)")
    private Integer isEnable;

    @Schema(description = "排序字段:sort=自定义排序优先(升序)+创建时间倒序(默认);createTime=创建时间倒序;accessCount=浏览数倒序;duration=时长倒序")
    private String sortField;
}
