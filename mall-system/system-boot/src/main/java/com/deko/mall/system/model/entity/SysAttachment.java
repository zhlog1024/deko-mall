package com.deko.mall.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.deko.mall.common.base.BaseEntity;
import lombok.Data;

/**
 * 附件信息表
 */
@Data
@TableName("sys_attachment")
public class SysAttachment extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 附件名称
     */
    private String name;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件路径（OSS路径）
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件类型（image/video/file）
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * 分类ID
     */
    private Long categoryId;
}

