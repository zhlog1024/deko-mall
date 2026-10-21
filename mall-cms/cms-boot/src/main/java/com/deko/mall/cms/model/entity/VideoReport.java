package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频举报实体
 */
@Data
@TableName("cms_video_report")
public class VideoReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 举报用户ID
     */
    private Long userId;

    /**
     * 举报类型(色情低俗/侵权/诈骗/其他)
     */
    private String type;

    /**
     * 举报说明
     */
    private String content;

    /**
     * 处理状态(0待处理/1已处理)
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer deleted;
}
