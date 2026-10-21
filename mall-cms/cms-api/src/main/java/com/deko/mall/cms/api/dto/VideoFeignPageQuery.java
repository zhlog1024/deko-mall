package com.deko.mall.cms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频分页查询（Feign，供装修器 plugins-video-list/tabs 取数）
 */
@Data
@Schema(description = "视频分页查询")
public class VideoFeignPageQuery implements Serializable {

    @Schema(description = "页码", example = "1")
    private int pageNum = 1;

    @Schema(description = "每页记录数", example = "10")
    private int pageSize = 10;

    @Schema(description = "关键字(标题模糊)")
    private String keyword;

    @Schema(description = "是否推荐(1是/0否,空=全部)")
    private Integer isRecommended;

    @Schema(description = "是否热门(1是/0否,空=全部)")
    private Integer isHot;

    @Schema(description = "是否启用(1是/0否,空=默认1)")
    private Integer isEnable;

    @Schema(description = "排序字段:sort=自定义排序优先(升序)+创建时间倒序(默认);createTime=创建时间;accessCount=浏览数;duration=时长;DIY orderByType 已在此映射,若直传 sortField 与 orderByType 并存以 orderByType 映射结果为准")
    private String sortField;

    @Schema(description = "排序规则:0=降序(默认)/1=升序;对 sortField=createTime/accessCount/duration 生效;综合(sortField 空或 sort)固定 sort asc+创建时间倒序,不受规则影响")
    private Integer orderByRule;
}
