package com.deko.mall.cms.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频详情响应（video 插件 detail 页消费结构）
 * <p>
 * 对应源 detail 响应 data 内的条目字段：video_url/cover/title/access_count/
 * give_thumbs_count/is_give_thumbs/comments_count/comments_list/goods 等。
 */
@Data
@Schema(description = "视频条目")
public class VideoPluginItemVO {

    @Schema(description = "视频ID")
    private Long id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "封面")
    private String cover;
    @Schema(description = "播放地址")
    private String videoUrl;
    @Schema(description = "简介")
    private String description;
    @Schema(description = "时长(秒)")
    private Integer duration;
    @Schema(description = "浏览数")
    private Integer accessCount;
    @Schema(description = "是否推荐/热门(1是)")
    private Integer isRecommended;
    private Integer isHot;
    @Schema(description = "点赞数")
    private Integer giveThumbsCount;
    @Schema(description = "当前用户是否已赞(登录态;游客 false)")
    private Boolean isGiveThumbs;
    @Schema(description = "评论总数")
    private Integer commentsCount;
    @Schema(description = "评论列表(树,懒加载顶层分页时可为空由 data 端点拉取)")
    private List<VideoPluginCommentVO> commentsList = new ArrayList<>();

    @Schema(description = "关联商品快照(挂商品时)")
    private VideoPluginGoodsVO goods;

    @Schema(description = "是否显示商品卡(由 base_config is_video_detail_show_goods_modal 驱动,前端可覆盖)")
    private Boolean showGoods;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Data
    @Schema(description = "关联商品快照")
    public static class VideoPluginGoodsVO {
        private Long id;
        private String title;
        private String images;
        private String price;
    }
}
