package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.dto.FreightCalcParam;
import com.deko.mall.pms.model.dto.FreightTemplateDTO;
import com.deko.mall.pms.model.entity.PmsFreightTemplate;
import com.deko.mall.pms.model.form.FreightTemplateForm;
import com.deko.mall.pms.model.query.FreightTemplateQuery;
import com.deko.mall.pms.model.vo.FreightTemplateVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运费模板服务接口
 */
public interface FreightTemplateService extends IService<PmsFreightTemplate> {

    /**
     * 分页查询运费模板
     *
     * @param query 查询条件
     * @return 分页数据
     */
    IPage<FreightTemplateVO> pageFreightTemplates(FreightTemplateQuery query);

    /**
     * 获取运费模板详情
     *
     * @param id 模板ID
     * @return 运费模板详情
     */
    FreightTemplateVO getFreightTemplateDetail(Long id);

    /**
     * 创建运费模板
     *
     * @param form 表单数据
     * @return 是否成功
     */
    boolean createFreightTemplate(FreightTemplateForm form);

    /**
     * 更新运费模板
     *
     * @param id   模板ID
     * @param form 表单数据
     * @return 是否成功
     */
    boolean updateFreightTemplate(Long id, FreightTemplateForm form);

    /**
     * 删除运费模板
     *
     * @param ids 模板ID列表
     * @return 是否成功
     */
    boolean deleteFreightTemplates(List<Long> ids);

    /**
     * 获取运费模板列表
     *
     * @return 运费模板列表
     */
    List<FreightTemplateVO> getFreightTemplateList(Long supplierId);

    /**
     * 设为默认模板
     *
     * @param id 模板ID
     * @return 是否成功
     */
    boolean setDefaultTemplate(Long id);

    /**
     * 获取供应商的默认运费模板
     *
     * @param supplierId 供应商ID
     * @return 默认运费模板详情
     */
    FreightTemplateVO getDefaultTemplate(Long supplierId);

    /**
     * 批量获取运费模板信息
     *
     * @param ids 运费模板ID集合
     * @return 运费模板DTO列表
     */
    List<FreightTemplateDTO> getFreightTemplatesByIds(List<Long> ids);

    /**
     * 计算运费
     *
     * @param param 运费计算参数
     * @return 运费金额(单位：元)
     */
    BigDecimal calculateFreight(FreightCalcParam param);
}
