package com.deko.mall.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.system.mapper.SysAttachmentCategoryMapper;
import com.deko.mall.system.mapper.SysAttachmentMapper;
import com.deko.mall.system.model.entity.SysAttachment;
import com.deko.mall.system.model.entity.SysAttachmentCategory;
import com.deko.mall.system.model.form.SysAttachmentForm;
import com.deko.mall.system.model.query.SysAttachmentQuery;
import com.deko.mall.system.model.vo.SysAttachmentItemVO;
import com.deko.mall.system.service.SysAttachmentService;
import com.deko.mall.system.service.storage.SysFileStorageFactory;
import com.deko.mall.system.service.storage.SysFileStorageService;
import com.deko.mall.system.vo.FileInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 附件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAttachmentServiceImpl extends ServiceImpl<SysAttachmentMapper, SysAttachment>
        implements SysAttachmentService {

    private final SysFileStorageFactory sysFileStorageFactory;
    private final SysAttachmentCategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfoVO uploadAttachment(MultipartFile file, Long categoryId) {
        // 1. 调用OSS上传
        SysFileStorageService storageService = sysFileStorageFactory.getDefaultFileStorageService();
        FileInfoVO fileInfo = storageService.uploadFile(file);

        // 2. 保存附件信息到数据库
        SysAttachment attachment = new SysAttachment();
        attachment.setName(file.getOriginalFilename());
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setFilePath(fileInfo.getName()); // OSS路径
        attachment.setFileUrl(fileInfo.getUrl()); // 访问URL
        attachment.setFileType(getFileType(file.getContentType()));
        attachment.setFileSize(file.getSize());
        attachment.setFileExt(getFileExt(file.getOriginalFilename()));
        attachment.setCategoryId(categoryId);

        this.save(attachment);

        return fileInfo;
    }

    @Override
    public IPage<SysAttachmentItemVO> getAttachmentList(SysAttachmentQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<SysAttachment> wrapper = new LambdaQueryWrapper<SysAttachment>()
                .like(StrUtil.isNotBlank(query.getKeywords()), SysAttachment::getName, query.getKeywords())
                .or(StrUtil.isNotBlank(query.getKeywords()), w -> w
                        .like(SysAttachment::getOriginalName, query.getKeywords()))
                .eq(query.getCategoryId() != null, SysAttachment::getCategoryId, query.getCategoryId())
                .eq(StrUtil.isNotBlank(query.getFileType()), SysAttachment::getFileType, query.getFileType())
                .orderByDesc(SysAttachment::getCreateTime);

        // 分页查询
        Page<SysAttachment> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<SysAttachment> pageResult = this.page(page, wrapper);

        // 转换为VO
        IPage<SysAttachmentItemVO> voPage = pageResult.convert(attachment -> {
            SysAttachmentItemVO vo = new SysAttachmentItemVO();
            BeanUtils.copyProperties(attachment, vo);

            // 查询分类名称
            if (attachment.getCategoryId() != null) {
                SysAttachmentCategory category = categoryMapper.selectById(attachment.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }

            return vo;
        });

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttachment(SysAttachmentForm form) {
        SysAttachment attachment = this.getById(form.getId());
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }

        // 更新附件信息
        if (StrUtil.isNotBlank(form.getName())) {
            attachment.setName(form.getName());
        }
        if (form.getCategoryId() != null) {
            attachment.setCategoryId(form.getCategoryId());
        }

        this.updateById(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long id) {
        SysAttachment attachment = this.getById(id);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }

        // 删除OSS文件
        SysFileStorageService storageService = sysFileStorageFactory.getDefaultFileStorageService();
        try {
            storageService.deleteFile(attachment.getFileUrl());
        } catch (Exception e) {
            // 记录日志，但不影响数据库删除
            log.warn("删除OSS文件失败: {}", attachment.getFileUrl(), e);
        }

        // 删除数据库记录
        this.removeById(id);
    }

    /**
     * 根据ContentType判断文件类型
     */
    private String getFileType(String contentType) {
        if (contentType == null) {
            return "file";
        }
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else {
            return "file";
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExt(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
