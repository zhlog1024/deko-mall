package com.deko.mall.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.cms.mapper.AgreementMapper;
import com.deko.mall.cms.model.dto.AgreementSaveDTO;
import com.deko.mall.cms.model.entity.Agreement;
import com.deko.mall.cms.model.query.AgreementPageQuery;
import com.deko.mall.cms.model.vo.AgreementVO;
import com.deko.mall.cms.service.AgreementService;
import com.deko.mall.common.web.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 协议服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgreementServiceImpl extends ServiceImpl<AgreementMapper, Agreement> implements AgreementService {

    @Override
    public IPage<AgreementVO> pageAgreements(AgreementPageQuery query) {
        Page<Agreement> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Agreement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agreement::getDeleted, 0);
        if (StringUtils.isNotBlank(query.getKeywords())) {
            String kw = query.getKeywords();
            wrapper.and(w -> w.like(Agreement::getDocument, kw).or().like(Agreement::getTitle, kw));
        }
        wrapper.orderByDesc(Agreement::getId);

        IPage<Agreement> result = page(page, wrapper);
        List<AgreementVO> voList = result.getRecords().stream().map(this::toVO).toList();
        Page<AgreementVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AgreementVO getDetail(Long id) {
        Agreement agreement = requireAgreement(id);
        return toVO(agreement);
    }

    @Override
    public boolean saveAgreement(AgreementSaveDTO dto) {
        checkDocumentUnique(dto.getDocument(), null);
        Agreement agreement = new Agreement();
        BeanUtils.copyProperties(dto, agreement);
        if (agreement.getStatus() == null) {
            agreement.setStatus(1);
        }
        agreement.setVersion(1);
        agreement.setDeleted(0);
        return save(agreement);
    }

    @Override
    public boolean updateAgreement(AgreementSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("协议ID不能为空");
        }
        requireAgreement(dto.getId());
        checkDocumentUnique(dto.getDocument(), dto.getId());
        Agreement agreement = new Agreement();
        BeanUtils.copyProperties(dto, agreement);
        return updateById(agreement);
    }

    @Override
    public boolean deleteAgreement(Long id) {
        Agreement upd = new Agreement();
        upd.setId(id);
        upd.setDeleted(1);
        return updateById(upd);
    }

    @Override
    public AgreementVO getEnabledByDocument(String document) {
        Agreement agreement = lambdaQuery().eq(Agreement::getDocument, document)
                .eq(Agreement::getStatus, 1)
                .eq(Agreement::getDeleted, 0)
                .last("LIMIT 1").one();
        if (agreement == null) {
            throw new BizException("协议不存在或未启用");
        }
        return toVO(agreement);
    }

    // 私有工具

    private Agreement requireAgreement(Long id) {
        Agreement agreement = getById(id);
        if (agreement == null || agreement.getDeleted() != null && agreement.getDeleted() == 1) {
            throw new BizException("协议不存在");
        }
        return agreement;
    }

    private void checkDocumentUnique(String document, Long excludeId) {
        if (StringUtils.isBlank(document)) {
            return;
        }
        Long count = lambdaQuery().eq(Agreement::getDocument, document)
                .eq(Agreement::getDeleted, 0)
                .ne(excludeId != null, Agreement::getId, excludeId)
                .count();
        if (count != null && count > 0) {
            throw new BizException("协议标识已存在：" + document);
        }
    }

    private AgreementVO toVO(Agreement agreement) {
        AgreementVO vo = new AgreementVO();
        BeanUtils.copyProperties(agreement, vo);
        return vo;
    }
}
