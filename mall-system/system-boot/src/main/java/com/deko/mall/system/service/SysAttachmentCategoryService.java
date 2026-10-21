package com.deko.mall.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.system.model.entity.SysAttachmentCategory;
import com.deko.mall.system.model.form.SysAttachmentCategoryForm;
import com.deko.mall.system.model.vo.SysAttachmentCategoryVO;

import java.util.List;

/**
 * 附件分类服务接口
 */
public interface SysAttachmentCategoryService extends IService<SysAttachmentCategory> {

    /**
     * 获取分类树
     *
     * @return 分类树列表
     */
    List<SysAttachmentCategoryVO> getCategoryTree();

    /**
     * 保存分类（新增/编辑）
     *
     * @param form 分类表单
     * @return 分类ID
     */
    Long saveCategory(SysAttachmentCategoryForm form);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 移动附件到其他分类
     *
     * @param attachmentIds 附件ID列表
     * @param targetCategoryId 目标分类ID
     */
    void moveAttachmentsToCategory(List<Long> attachmentIds, Long targetCategoryId);
}
