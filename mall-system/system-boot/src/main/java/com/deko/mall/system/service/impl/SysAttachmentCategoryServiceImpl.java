package com.deko.mall.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.enums.StatusEnum;
import com.deko.mall.system.mapper.SysAttachmentCategoryMapper;
import com.deko.mall.system.mapper.SysAttachmentMapper;
import com.deko.mall.system.model.entity.SysAttachment;
import com.deko.mall.system.model.entity.SysAttachmentCategory;
import com.deko.mall.system.model.form.SysAttachmentCategoryForm;
import com.deko.mall.system.model.vo.SysAttachmentCategoryVO;
import com.deko.mall.system.service.SysAttachmentCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 附件分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysAttachmentCategoryServiceImpl extends ServiceImpl<SysAttachmentCategoryMapper, SysAttachmentCategory>
        implements SysAttachmentCategoryService {

    private final SysAttachmentMapper attachmentMapper;

    @Override
    public List<SysAttachmentCategoryVO> getCategoryTree() {
        // 查询所有启用的分类
        List<SysAttachmentCategory> categoryList = this.list(
                new LambdaQueryWrapper<SysAttachmentCategory>()
                        .eq(SysAttachmentCategory::getIsEnabled, StatusEnum.ENABLE.getValue())
                        .orderByAsc(SysAttachmentCategory::getSort)
                        .orderByAsc(SysAttachmentCategory::getId)
        );

        // 转换为VO
        List<SysAttachmentCategoryVO> voList = categoryList.stream()
                .map(category -> {
                    SysAttachmentCategoryVO vo = new SysAttachmentCategoryVO();
                    BeanUtils.copyProperties(category, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(voList, 0L);
    }

    /**
     * 递归构建树形结构
     */
    private List<SysAttachmentCategoryVO> buildTree(List<SysAttachmentCategoryVO> allList, Long parentId) {
        List<SysAttachmentCategoryVO> result = new ArrayList<>();
        for (SysAttachmentCategoryVO vo : allList) {
            if (vo.getParentId() != null && vo.getParentId().equals(parentId)) {
                List<SysAttachmentCategoryVO> children = buildTree(allList, vo.getId());
                if (CollectionUtil.isNotEmpty(children)) {
                    vo.setChildren(children);
                }
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveCategory(SysAttachmentCategoryForm form) {
        SysAttachmentCategory category = new SysAttachmentCategory();
        BeanUtils.copyProperties(form, category);

        if (form.getId() == null) {
            // 新增
            if (form.getParentId() == null) {
                category.setParentId(0L);
            }
            if (form.getIsEnabled() == null) {
                category.setIsEnabled(StatusEnum.ENABLE.getValue());
            }
            if (form.getSort() == null) {
                category.setSort(0);
            }
            this.save(category);
            return category.getId();
        } else {
            // 更新
            category.setId(form.getId());
            this.updateById(category);
            return category.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        long childCount = this.count(
                new LambdaQueryWrapper<SysAttachmentCategory>()
                        .eq(SysAttachmentCategory::getParentId, id)
        );
        if (childCount > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }

        // 检查是否有附件
        long attachmentCount = attachmentMapper.selectCount(
                new LambdaQueryWrapper<SysAttachment>()
                        .eq(SysAttachment::getCategoryId, id)
        );
        if (attachmentCount > 0) {
            throw new RuntimeException("该分类下存在附件，无法删除");
        }

        // 删除分类
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveAttachmentsToCategory(List<Long> attachmentIds, Long targetCategoryId) {
        if (CollectionUtil.isEmpty(attachmentIds)) {
            return;
        }

        // 批量更新附件的分类ID
        for (Long attachmentId : attachmentIds) {
            SysAttachment attachment = attachmentMapper.selectById(attachmentId);
            if (attachment != null) {
                attachment.setCategoryId(targetCategoryId);
                attachmentMapper.updateById(attachment);
            }
        }
    }
}

