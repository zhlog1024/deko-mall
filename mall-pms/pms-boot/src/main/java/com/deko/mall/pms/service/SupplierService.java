package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.dto.SupplierDTO;
import com.deko.mall.pms.model.entity.PmsSupplier;
import com.deko.mall.pms.model.form.SupplierAuditForm;
import com.deko.mall.pms.model.form.SupplierForm;
import com.deko.mall.pms.model.query.SupplierPageQuery;
import com.deko.mall.pms.model.vo.SupplierVO;

/**
 * 供应商服务接口
 */
public interface SupplierService extends IService<PmsSupplier> {

    /**
     * 获取供应商分页列表
     *
     * @param queryParams 查询参数
     * @return 供应商分页列表
     */
    IPage<SupplierVO> getSupplierPages(SupplierPageQuery queryParams);

    /**
     * 获取供应商详情
     *
     * @param id 供应商ID
     * @return 供应商详情
     */
    SupplierVO getSupplierById(Long id);


    /**
     * 获取供应商详情
     *
     * @param userId 用户ID
     * @return 供应商详情
     */
    SupplierDTO getSupplierByUserId(Long userId);

    /**
     * 新增供应商
     *
     * @param supplierForm 供应商表单
     * @return 是否成功
     */
    boolean addSupplier(SupplierForm supplierForm);

    /**
     * 修改供应商
     *
     * @param id           供应商ID
     * @param supplierForm 供应商表单
     * @return 是否成功
     */
    boolean updateSupplier(Long id, SupplierForm supplierForm);

    /**
     * 删除供应商
     *
     * @param ids 供应商ID，多个以英文逗号(,)分隔
     * @return 是否成功
     */
    boolean deleteSuppliers(String ids);

    /**
     * 审核供应商
     *
     * @param id        供应商ID
     * @param auditForm 审核表单
     * @return 是否成功
     */
    boolean auditSupplier(Long id, SupplierAuditForm auditForm);

    /**
     * 生成供应商编码
     *
     * @return 供应商编码
     */
    String generateSupplierCode();
}
