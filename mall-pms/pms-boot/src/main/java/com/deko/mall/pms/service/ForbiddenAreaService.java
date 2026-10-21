package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.api.ForbiddenAreaFeignClient.ForbiddenAreaCheckRequest;
import com.deko.mall.pms.model.entity.PmsForbiddenArea;
import com.deko.mall.pms.model.form.ForbiddenAreaForm;
import com.deko.mall.pms.model.query.ForbiddenAreaQuery;
import com.deko.mall.pms.model.vo.ForbiddenAreaVO;

import java.util.List;

/**
 * 禁售区域服务接口
 */
public interface ForbiddenAreaService extends IService<PmsForbiddenArea> {

    /**
     * 分页查询禁售区域
     *
     * @param query 查询条件
     * @return 分页数据
     */
    IPage<ForbiddenAreaVO> pageForbiddenAreas(ForbiddenAreaQuery query);

    /**
     * 获取禁售区域详情
     *
     * @param id 禁售区域ID
     * @return 禁售区域详情
     */
    ForbiddenAreaVO getForbiddenAreaDetail(Long id);

    /**
     * 创建禁售区域
     *
     * @param form 表单数据
     * @return 是否成功
     */
    boolean createForbiddenArea(ForbiddenAreaForm form);

    /**
     * 更新禁售区域
     *
     * @param id   禁售区域ID
     * @param form 表单数据
     * @return 是否成功
     */
    boolean updateForbiddenArea(Long id, ForbiddenAreaForm form);

    /**
     * 删除禁售区域
     *
     * @param ids 禁售区域ID列表
     * @return 是否成功
     */
    boolean deleteForbiddenAreas(List<Long> ids);

    /**
     * 获取禁售区域列表
     *
     * @return 禁售区域列表
     */
    List<ForbiddenAreaVO> getForbiddenAreaList(Long supplierId);

    /**
     * 检查指定区域是否在禁售区域内
     *
     * @param regionCode 区域编码
     * @param supplierId 供应商ID
     * @return 是否在禁售区域内
     */
    boolean isInForbiddenArea(String regionCode, Long supplierId);

    /**
     * 批量检查商品在指定区域是否禁售
     *
     * @param request 请求参数，包含商品SPU ID列表和地区信息
     * @return 禁售的商品SPU ID列表
     */
    List<Long> batchCheckForbiddenProducts(ForbiddenAreaCheckRequest request);

    /**
     * 检查区域编码是否在指定的禁售区域列表中
     *
     * @param regionCode       区域编码
     * @param forbiddenAreaIds 禁售区域ID列表
     * @return 包含该区域编码的禁售区域ID列表
     */
    List<Long> checkRegionInForbiddenAreas(String regionCode, List<Long> forbiddenAreaIds);
}
