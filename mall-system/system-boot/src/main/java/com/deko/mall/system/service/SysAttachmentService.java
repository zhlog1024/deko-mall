package com.deko.mall.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.system.model.entity.SysAttachment;
import com.deko.mall.system.model.form.SysAttachmentForm;
import com.deko.mall.system.model.query.SysAttachmentQuery;
import com.deko.mall.system.model.vo.SysAttachmentItemVO;
import com.deko.mall.system.vo.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件服务接口
 */
public interface SysAttachmentService extends IService<SysAttachment> {

    /**
     * 上传附件（复用OSS上传）
     *
     * @param file 文件
     * @param categoryId 分类ID
     * @return 附件信息
     */
    FileInfoVO uploadAttachment(MultipartFile file, Long categoryId);

    /**
     * 获取附件列表（分页）
     *
     * @param query 查询条件
     * @return 附件列表
     */
    IPage<SysAttachmentItemVO> getAttachmentList(SysAttachmentQuery query);

    /**
     * 保存附件信息（重命名等）
     *
     * @param form 附件表单
     */
    void saveAttachment(SysAttachmentForm form);

    /**
     * 删除附件
     *
     * @param id 附件ID
     */
    void deleteAttachment(Long id);
}
