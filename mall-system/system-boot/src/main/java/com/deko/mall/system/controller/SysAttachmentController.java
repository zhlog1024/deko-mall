package com.deko.mall.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.system.model.entity.SysAttachment;
import com.deko.mall.system.model.form.SysAttachmentForm;
import com.deko.mall.system.model.query.SysAttachmentQuery;
import com.deko.mall.system.model.vo.SysAttachmentItemVO;
import com.deko.mall.system.service.SysAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件控制器
 */
@Tag(name = "附件管理")
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class SysAttachmentController {

    private final SysAttachmentService attachmentService;

    @Operation(summary = "获取附件列表（分页）")
    @GetMapping
    public PageResult<SysAttachmentItemVO> getAttachmentList(@ParameterObject SysAttachmentQuery query) {
        IPage<SysAttachmentItemVO> page = attachmentService.getAttachmentList(query);
        return PageResult.success(page);
    }

    @Operation(summary = "上传附件")
    @PostMapping("/upload")
    public Result<SysAttachmentItemVO> uploadAttachment(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId
    ) {
        // 上传文件并保存附件信息
        com.deko.mall.system.vo.FileInfoVO fileInfo = attachmentService.uploadAttachment(file, categoryId);

        // 查询刚上传的附件信息
        SysAttachment attachment = attachmentService.getOne(
                new LambdaQueryWrapper<SysAttachment>()
                        .eq(SysAttachment::getFilePath, fileInfo.getName())
                        .orderByDesc(SysAttachment::getCreateTime)
                        .last("LIMIT 1")
        );

        if (attachment != null) {
            SysAttachmentItemVO vo = new SysAttachmentItemVO();
            vo.setId(attachment.getId());
            vo.setName(attachment.getName());
            vo.setOriginalName(attachment.getOriginalName());
            vo.setFilePath(attachment.getFilePath());
            vo.setFileUrl(attachment.getFileUrl());
            vo.setFileType(attachment.getFileType());
            vo.setFileSize(attachment.getFileSize());
            vo.setFileExt(attachment.getFileExt());
            vo.setCategoryId(attachment.getCategoryId());
            return Result.success(vo);
        }

        return Result.success();
    }

    @Operation(summary = "更新附件信息（重命名等）")
    @PutMapping("/{id}")
    public Result<Void> updateAttachment(
            @Parameter(description = "附件ID") @PathVariable Long id,
            @Valid @RequestBody SysAttachmentForm form
    ) {
        form.setId(id);
        attachmentService.saveAttachment(form);
        return Result.success();
    }

    @Operation(summary = "删除附件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAttachment(
            @Parameter(description = "附件ID") @PathVariable Long id
    ) {
        attachmentService.deleteAttachment(id);
        return Result.success();
    }
}
