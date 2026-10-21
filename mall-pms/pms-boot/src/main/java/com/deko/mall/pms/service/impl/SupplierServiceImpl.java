package com.deko.mall.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pms.config.PmsResultCode;
import com.deko.mall.pms.converter.SupplierConverter;
import com.deko.mall.pms.enums.SupplierAuditStatusEnum;
import com.deko.mall.pms.mapper.PmsSupplierMapper;
import com.deko.mall.pms.model.dto.SupplierDTO;
import com.deko.mall.pms.model.entity.PmsSupplier;
import com.deko.mall.pms.model.form.SupplierAuditForm;
import com.deko.mall.pms.model.form.SupplierForm;
import com.deko.mall.pms.model.query.SupplierPageQuery;
import com.deko.mall.pms.model.vo.SupplierVO;
import com.deko.mall.pms.service.ShopService;
import com.deko.mall.pms.service.SupplierService;
import com.deko.mall.system.api.SystemFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应商服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<PmsSupplierMapper, PmsSupplier> implements SupplierService {

    private final PmsSupplierMapper pmsSupplierMapper;
    private final SupplierConverter supplierConverter;

    private final SystemFeignClient systemFeignClient;

    private final ShopService shopService;

    @Override
    public IPage<SupplierVO> getSupplierPages(SupplierPageQuery queryParams) {
        Page<PmsSupplier> supplierPages = pmsSupplierMapper.getSupplierPages(new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams);
        Page<SupplierVO> supplierVOPage = supplierConverter.entity2Page(supplierPages);

        return supplierVOPage;
    }

    @Override
    public SupplierVO getSupplierById(Long id) {
        // 获取供应商信息
        PmsSupplier supplier = getById(id);
        if (supplier == null) {
            throw new BizException(PmsResultCode.SUPPLIER_NOT_FOUND);
        }

        // 转换为详情VO
        SupplierVO supplierVO = supplierConverter.entity2VO(supplier);

        return supplierVO;
    }

    @Override
    public SupplierDTO getSupplierByUserId(Long userId) {

        // 获取供应商信息
        PmsSupplier supplier = this.getBaseMapper().selectOne(new LambdaQueryWrapper<PmsSupplier>().
                eq(PmsSupplier::getUserId, userId));
        if (supplier == null) {
            throw new BizException(PmsResultCode.SUPPLIER_NOT_FOUND);
        }
        // 转换为详情VO
        SupplierDTO supplierDTO = supplierConverter.entity2DTO(supplier);

        return supplierDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSupplier(SupplierForm supplierForm) {
        //校验密码和确认密码一致
        Assert.isTrue(StrUtil.equals(supplierForm.getPassword(), supplierForm.getConfirmPassword()), "密码和确认密码不一致");

        // 检查供应商名称是否重复
        long count = count(new LambdaQueryWrapper<PmsSupplier>()
                .eq(PmsSupplier::getSupplierName, supplierForm.getSupplierName()));
        Assert.isTrue(count == 0, "供应商名称已存在");

        // 表单转换为实体
        PmsSupplier supplier = supplierConverter.form2Entity(supplierForm);

        // 设置初始状态为待审核
        supplier.setAuditStatus(SupplierAuditStatusEnum.PENDING.getValue());

        // 生成供应商编码
        supplier.setSupplierCode(generateSupplierCode());

        // 保存供应商信息
        boolean saved = save(supplier);

        // 供应商保存成功后，调用系统服务分配权限。
        Result<Long> result = null;
        if (saved) {
            try {
                log.info("开始创建供应商用户: supplierId={}, supplierName={}", supplier.getId(), supplier.getSupplierName());
                result = systemFeignClient.createPermissionUser(
                        supplierForm.getLoginName(), supplierForm.getSupplierName(), supplierForm.getPassword()
                );

                if (!ResultCode.SUCCESS.getCode().equals(result.getCode())) {
                    log.error("创建供应商失败: {}", result.getMsg());
                    throw new BizException(PmsResultCode.SYSTEM_ERROR, "创建供应商失败: " + result.getMsg());
                }

                log.info("供应商权限分配成功: supplierId={}", supplier.getId());
            } catch (Exception e) {
                log.error("调用权限服务异常", e);
                throw new BizException(PmsResultCode.SYSTEM_ERROR, "创建供应商失败: " + e.getMessage());
            }
        }

        Long userId = result.getData();
        supplier.setUserId(userId);
        this.updateById(supplier);

        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSupplier(Long id, SupplierForm supplierForm) {
        // 检查供应商是否存在
        PmsSupplier supplier = getById(id);
        if (supplier == null) {
            throw new BizException(PmsResultCode.SUPPLIER_NOT_FOUND);
        }

        // 检查供应商名称是否重复（排除自身）
        long count = count(new LambdaQueryWrapper<PmsSupplier>()
                .eq(PmsSupplier::getSupplierName, supplierForm.getSupplierName())
                .ne(PmsSupplier::getId, id));
        Assert.isTrue(count == 0, "供应商名称已存在");

        // 表单转换为实体
        PmsSupplier updateSupplier = supplierConverter.form2Entity(supplierForm);
        updateSupplier.setId(id);
        updateSupplier.setSupplierCode(supplier.getSupplierCode());
        updateSupplier.setAuditStatus(supplier.getAuditStatus());

        // 更新供应商信息
        boolean supplierUpdated = updateById(updateSupplier);
        if (!supplierUpdated) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSuppliers(String ids) {
        if (StrUtil.isBlank(ids)) {
            return false;
        }

        // 解析ID列表
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(idList)) {
            return false;
        }

        // 删除供应商信息
        return removeByIds(idList);
    }


    @Override
    public boolean auditSupplier(Long id, SupplierAuditForm auditForm) {
        // 检查供应商是否存在
        PmsSupplier supplier = getById(id);
        if (supplier == null) {
            throw new BizException(PmsResultCode.SUPPLIER_NOT_FOUND);
        }

        // 只有待审核状态才能进行审核
        if (!SupplierAuditStatusEnum.PENDING.getValue().equals(supplier.getAuditStatus())) {
            throw new BizException(PmsResultCode.SUPPLIER_AUDIT_STATUS_INVALID);
        }


        // 更新审核状态
        PmsSupplier updateSupplier = new PmsSupplier();
        updateSupplier.setId(id);
        updateSupplier.setAuditStatus(auditForm.getAuditStatus());
        updateSupplier.setRemark(auditForm.getRemark());
        boolean update = updateById(updateSupplier);
        if (update) {
            if (SupplierAuditStatusEnum.APPROVED.getValue().equals(auditForm.getAuditStatus())) {
                // 审核通过，调用系统服务分配权限
                try {
                    log.info("开始为供应商分配权限: supplierId={}, supplierName={}", supplier.getId(), supplier.getSupplierName());
                    Result<Long> result = systemFeignClient.assignPermission(supplier.getUserId());

                    if (!ResultCode.SUCCESS.getCode().equals(result.getCode())) {
                        log.error("为供应商分配权限失败: {}", result.getMsg());
                        throw new BizException(PmsResultCode.SYSTEM_ERROR, "为供应商分配权限失败: " + result.getMsg());
                    }
                } catch (Exception e) {
                    log.error("调用权限服务异常", e);
                    throw new BizException(PmsResultCode.SYSTEM_ERROR, "为供应商分配权限失败: " + e.getMessage());
                }

                // 审核通过，自动创建店铺（带入入驻时填写的店铺名称/logo/简介）
                try {
                    String shopName = StrUtil.isBlank(supplier.getShopName())
                            ? supplier.getSupplierName() : supplier.getShopName();
                    shopService.createShop("SUPPLIER", supplier.getId(), shopName,
                            supplier.getShopLogo(), supplier.getShopDescription());
                } catch (Exception e) {
                    log.error("供应商审核通过创建店铺失败: supplierId={}", supplier.getId(), e);
                }

            }
        }
        return true;
    }

    @Override
    public String generateSupplierCode() {
        // 生成格式：SP + 年月日 + 4位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = RandomUtil.randomNumbers(4);
        return "SP" + dateStr + randomStr;
    }
}
