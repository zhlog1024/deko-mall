package com.deko.mall.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.cms.model.dto.AgreementSaveDTO;
import com.deko.mall.cms.model.entity.Agreement;
import com.deko.mall.cms.model.query.AgreementPageQuery;
import com.deko.mall.cms.model.vo.AgreementVO;

/**
 * 协议服务。
 */
public interface AgreementService extends IService<Agreement> {

    /**
     * 管理端分页
     */
    IPage<AgreementVO> pageAgreements(AgreementPageQuery query);

    /**
     * 管理端详情
     */
    AgreementVO getDetail(Long id);

    /**
     * 新增协议
     */
    boolean saveAgreement(AgreementSaveDTO dto);

    /**
     * 编辑协议
     */
    boolean updateAgreement(AgreementSaveDTO dto);

    /**
     * 删除协议（逻辑删除）
     */
    boolean deleteAgreement(Long id);

    /**
     * App 端按 document 取启用协议
     */
    AgreementVO getEnabledByDocument(String document);
}
