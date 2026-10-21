package com.deko.mall.cms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商品问答实体
 */
@Data
@TableName("cms_ask")
@EqualsAndHashCode(callSuper = true)
public class Ask extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联商品SPU ID(0=不指定,DIY筛选全量时用)
     */
    private Long spuId;
    /**
     * 商品名称快照(写入时冗余,读取零Feign)
     */
    private String spuName;
    /**
     * 商品主图快照
     */
    private String spuPic;
    /**
     * 问题标题
     */
    private String title;
    /**
     * 问题详情(可空)
     */
    private String content;
    /**
     * 运营回答内容
     */
    private String answer;
    /**
     * 是否已回复(0否/1是)
     */
    private Integer isReply;
    /**
     * 浏览数
     */
    private Integer accessCount;
    /**
     * 提问人昵称快照(App提问用户;运营新增可空)
     */
    private String askUser;
    /**
     * 提问用户ID(App提问;运营新增为空)
     */
    private Long askUserId;
    /**
     * 回答人昵称快照(运营)
     */
    private String replyUser;
    /**
     * 回答时间
     */
    private LocalDateTime replyTime;

    /**
     * 排序(升序)
     */
    private Integer sort;
    /**
     * 是否启用(1是/0否)
     */
    private Integer isEnable;
    /**
     * 逻辑删除(0正常/1删除)
     */
    private Integer deleted;
}
