package com.deko.mall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pms.converter.FreightRuleConverter;
import com.deko.mall.pms.converter.FreightTemplateConverter;
import com.deko.mall.pms.enums.ChargeTypeEnum;
import com.deko.mall.pms.enums.YesNoEnum;
import com.deko.mall.pms.mapper.PmsFreightRuleMapper;
import com.deko.mall.pms.mapper.PmsFreightTemplateMapper;
import com.deko.mall.pms.model.dto.FreightCalcParam;
import com.deko.mall.pms.model.dto.FreightRuleDTO;
import com.deko.mall.pms.model.dto.FreightTemplateDTO;
import com.deko.mall.pms.model.entity.PmsFreightRule;
import com.deko.mall.pms.model.entity.PmsFreightTemplate;
import com.deko.mall.pms.model.form.FreightTemplateForm;
import com.deko.mall.pms.model.query.FreightTemplateQuery;
import com.deko.mall.pms.model.vo.FreightRuleVO;
import com.deko.mall.pms.model.vo.FreightTemplateVO;
import com.deko.mall.pms.service.FreightTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运费模板服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FreightTemplateServiceImpl extends ServiceImpl<PmsFreightTemplateMapper, PmsFreightTemplate> implements FreightTemplateService {

    // 固定值设置，实际可从配置表中读取
    private final PmsFreightRuleMapper freightRuleMapper;
    private final FreightTemplateConverter freightTemplateConverter;
    private final FreightRuleConverter freightRuleConverter;

    @Override
    public IPage<FreightTemplateVO> pageFreightTemplates(FreightTemplateQuery query) {

        LambdaQueryWrapper<PmsFreightTemplate> queryWrapper = new LambdaQueryWrapper<PmsFreightTemplate>()
                .eq(query.getChargeType() != null, PmsFreightTemplate::getChargeType, query.getChargeType())
                .eq(query.getIsFreeShipping() != null, PmsFreightTemplate::getIsFreeShipping, query.getIsFreeShipping())
                .eq(query.getIsDefault() != null, PmsFreightTemplate::getIsDefault, query.getIsDefault())
                .eq(query.getSupplierId() != null, PmsFreightTemplate::getSupplierId, query.getSupplierId())
                .like(StringUtils.hasText(query.getTemplateName()), PmsFreightTemplate::getTemplateName, query.getTemplateName())
                .orderByDesc(PmsFreightTemplate::getIsDefault)
                .orderByDesc(PmsFreightTemplate::getCreateTime);

        Page<PmsFreightTemplate> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<PmsFreightTemplate> resultPage = this.page(page, queryWrapper);
        Page<FreightTemplateVO> voPage = freightTemplateConverter.entity2Page(resultPage);
        // 填充运费规则
        voPage.getRecords().stream().forEach(vo -> {
            List<PmsFreightRule> rules = freightRuleMapper.selectList(new LambdaQueryWrapper<PmsFreightRule>()
                    .eq(PmsFreightRule::getTemplateId, vo.getId()));
            vo.setRules(freightRuleConverter.entities2VOs(rules));
        });
        return voPage;
    }

    @Override
    public FreightTemplateVO getFreightTemplateDetail(Long id) {
        PmsFreightTemplate template = this.getById(id);
        Assert.notNull(template, "运费模板不存在");

        FreightTemplateVO vo = this.convertToVO(template);

        // 查询运费规则
        LambdaQueryWrapper<PmsFreightRule> queryWrapper = new LambdaQueryWrapper<PmsFreightRule>()
                .eq(PmsFreightRule::getTemplateId, id);
        List<PmsFreightRule> rules = freightRuleMapper.selectList(queryWrapper);
        List<FreightRuleVO> freightRuleVOS = freightRuleConverter.entities2VOs(rules);
        vo.setRules(freightRuleVOS);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFreightTemplate(FreightTemplateForm form) {
        Long supplierId = form.getSupplierId();
        if (supplierId == null) {
            supplierId = SecurityUtils.getSupplierId();
        }

        // 检查是否为默认模板
        if (Integer.valueOf(1).equals(form.getIsDefault())) {
            this.resetDefaultTemplate(supplierId);
        }

        PmsFreightTemplate template = new PmsFreightTemplate();
        BeanUtils.copyProperties(form, template);
        template.setSupplierId(supplierId);

        boolean result = this.save(template);

        // 保存运费规则
        if (result && form.getIsFreeShipping() == 0 && !CollectionUtils.isEmpty(form.getRules())) {
            // 验证和处理默认规则，确保只有一个默认规则
            validateAndProcessDefaultRules(form.getRules());

            List<PmsFreightRule> rules = form.getRules().stream().map(rule -> {
                PmsFreightRule entity = new PmsFreightRule();
                BeanUtils.copyProperties(rule, entity);
                entity.setTemplateId(template.getId());
                return entity;
            }).collect(Collectors.toList());

            if (!rules.isEmpty()) {
                // 使用循环逐条插入，避免批量插入方法不存在的问题
                for (PmsFreightRule rule : rules) {
                    freightRuleMapper.insert(rule);
                }
                result = true;
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFreightTemplate(Long id, FreightTemplateForm form) {

        PmsFreightTemplate template = this.getById(id);
        Assert.notNull(template, "运费模板不存在");

        // 检查是否为默认模板
        if (Integer.valueOf(1).equals(form.getIsDefault()) && !Integer.valueOf(1).equals(template.getIsDefault())) {
            this.resetDefaultTemplate(template.getSupplierId());
        }


        BeanUtils.copyProperties(form, template);
        template.setId(id);

        boolean result = this.updateById(template);

        // 先删除原有规则
        LambdaQueryWrapper<PmsFreightRule> queryWrapper = new LambdaQueryWrapper<PmsFreightRule>().eq(PmsFreightRule::getTemplateId, id);
        freightRuleMapper.delete(queryWrapper);

        // 保存运费规则
        if (result && form.getIsFreeShipping() == 0 && !CollectionUtils.isEmpty(form.getRules())) {
            // 验证和处理默认规则，确保只有一个默认规则
            validateAndProcessDefaultRules(form.getRules());

            List<PmsFreightRule> rules = form.getRules().stream().map(rule -> {
                PmsFreightRule entity = new PmsFreightRule();
                BeanUtils.copyProperties(rule, entity);
                entity.setId(null);
                entity.setTemplateId(template.getId());
                return entity;
            }).collect(Collectors.toList());

            if (!rules.isEmpty()) {
                for (PmsFreightRule rule : rules) {
                    freightRuleMapper.insert(rule);
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFreightTemplates(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }

        // 检查是否包含默认模板
        List<PmsFreightTemplate> templates = this.listByIds(ids);
        for (PmsFreightTemplate template : templates) {
            if (YesNoEnum.YES.getValue().equals(template.getIsDefault())) {
                throw new IllegalArgumentException("默认模板不能删除");
            }
        }

        // 删除模板及其对应的规则
        boolean removed = this.removeByIds(ids);
        if (removed) {
            for (Long id : ids) {
                LambdaQueryWrapper<PmsFreightRule> queryWrapper = new LambdaQueryWrapper<PmsFreightRule>()
                        .eq(PmsFreightRule::getTemplateId, id);
                freightRuleMapper.delete(queryWrapper);
            }
        }

        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultTemplate(Long id) {
        PmsFreightTemplate template = this.getById(id);
        Assert.notNull(template, "运费模板不存在");

        // 先将其他默认模板取消默认
        this.resetDefaultTemplate(template.getSupplierId());

        // 设置为默认模板
        template.setIsDefault(YesNoEnum.YES.getValue());

        return this.updateById(template);
    }

    @Override
    public FreightTemplateVO getDefaultTemplate(Long supplierId) {

        LambdaQueryWrapper<PmsFreightTemplate> queryWrapper = new LambdaQueryWrapper<PmsFreightTemplate>()
                .eq(PmsFreightTemplate::getSupplierId, supplierId)
                .eq(PmsFreightTemplate::getIsDefault, YesNoEnum.YES.getValue());

        PmsFreightTemplate template = this.getOne(queryWrapper);
        if (template == null) {
            return null;
        }

        return this.getFreightTemplateDetail(template.getId());
    }

    @Override
    public List<FreightTemplateDTO> getFreightTemplatesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询运费模板基本信息
        List<PmsFreightTemplate> templates = this.lambdaQuery()
                .in(PmsFreightTemplate::getId, ids)
                .list();

        if (templates.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询关联的运费规则
        LambdaQueryWrapper<PmsFreightRule> queryWrapper = new LambdaQueryWrapper<PmsFreightRule>()
                .in(PmsFreightRule::getTemplateId, ids);
        List<PmsFreightRule> rules = freightRuleMapper.selectList(queryWrapper);

        // 按模板ID分组规则
        Map<Long, List<PmsFreightRule>> ruleMap = rules.stream()
                .collect(Collectors.groupingBy(PmsFreightRule::getTemplateId));

        // 转换为DTO对象
        return templates.stream().map(template -> {
            FreightTemplateDTO dto = new FreightTemplateDTO();
            BeanUtils.copyProperties(template, dto);

            // 设置运费规则列表
            List<PmsFreightRule> templateRules = ruleMap.getOrDefault(template.getId(), Collections.emptyList());
            List<FreightRuleDTO> ruleDTOs = new ArrayList<>();

            for (PmsFreightRule rule : templateRules) {
                FreightRuleDTO ruleDTO = new FreightRuleDTO();
                BeanUtils.copyProperties(rule, ruleDTO);
                ruleDTOs.add(ruleDTO);
            }

            dto.setRules(ruleDTOs);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FreightTemplateVO> getFreightTemplateList(Long supplierId) {

        LambdaQueryWrapper<PmsFreightTemplate> queryWrapper = new LambdaQueryWrapper<PmsFreightTemplate>()
                .eq(PmsFreightTemplate::getSupplierId, supplierId)
                .orderByDesc(PmsFreightTemplate::getIsDefault)
                .orderByDesc(PmsFreightTemplate::getUpdateTime);

        List<PmsFreightTemplate> templates = this.list(queryWrapper);

        return templates.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 重置供应商的默认模板
     *
     * @param supplierId 供应商ID
     */
    private void resetDefaultTemplate(Long supplierId) {
        LambdaQueryWrapper<PmsFreightTemplate> queryWrapper = new LambdaQueryWrapper<PmsFreightTemplate>()
                .eq(PmsFreightTemplate::getSupplierId, supplierId)
                .eq(PmsFreightTemplate::getIsDefault, YesNoEnum.YES.getValue());

        PmsFreightTemplate defaultTemplate = this.getOne(queryWrapper);
        if (defaultTemplate != null) {
            defaultTemplate.setIsDefault(YesNoEnum.NO.getValue());
        }
        this.updateById(defaultTemplate);
    }

    /**
     * 转换为VO对象
     *
     * @param template 运费模板实体
     * @return 运费模板VO
     */
    private FreightTemplateVO convertToVO(PmsFreightTemplate template) {
        FreightTemplateVO vo = new FreightTemplateVO();
        BeanUtils.copyProperties(template, vo);

        // 设置枚举描述
        vo.setChargeTypeLabel(ChargeTypeEnum.BY_PIECE.getValue().equals(template.getChargeType())
                ? ChargeTypeEnum.BY_PIECE.getLabel() : ChargeTypeEnum.BY_WEIGHT.getLabel());
        vo.setIsFreeShippingLabel(YesNoEnum.YES.getValue().equals(template.getIsFreeShipping())
                ? YesNoEnum.YES.getLabel() : YesNoEnum.NO.getLabel());
        vo.setIsDefaultLabel(YesNoEnum.YES.getValue().equals(template.getIsDefault())
                ? YesNoEnum.YES.getLabel() : YesNoEnum.NO.getLabel());

        return vo;
    }

    /**
     * 计算运费
     *
     * @param param 运费计算参数
     * @return 运费金额(单位：元)
     */
    @Override
    public BigDecimal calculateFreight(FreightCalcParam param) {
        if (param == null || param.getTemplateId() == null) {
            log.warn("运费计算参数缺失");
            return BigDecimal.ZERO;
        }

        try {
            // 获取运费模板信息
            List<FreightTemplateDTO> templates = this.getFreightTemplatesByIds(List.of(param.getTemplateId()));
            if (templates == null || templates.isEmpty()) {
                log.warn("运费模板不存在，templateId: {}", param.getTemplateId());
                return BigDecimal.ZERO;
            }

            FreightTemplateDTO template = templates.get(0);

            // 1. 判断是否包邮
            if (Integer.valueOf(1).equals(template.getIsFreeShipping())) {
                return BigDecimal.ZERO;
            }

            // 2. 计算运费
            return calculateFreightByTemplate(template, param);
        } catch (Exception e) {
            log.error("计算运费异常", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 根据运费模板和参数计算运费
     *
     * @param template 运费模板
     * @param param    运费计算参数
     * @return 运费金额(单位：元)
     */
    private BigDecimal calculateFreightByTemplate(FreightTemplateDTO template, FreightCalcParam param) {


        String chargeType = template.getChargeType();

        // 检查模板规则
        List<FreightRuleDTO> rules = template.getRules();

        // 尝试匹配收货地址所在区域的规则
        FreightRuleDTO matchedRule = null;

        // 先尝试精确匹配区/县级规则
        if (param.getDistrictCode() != null) {
            matchedRule = findMatchedRule(rules, param.getDistrictCode());
        }

        // 如果没有匹配到区级规则，尝试匹配市级规则
        if (matchedRule == null && param.getCityCode() != null) {
            matchedRule = findMatchedRule(rules, param.getCityCode());
        }

        // 如果没有匹配到市级规则，尝试匹配省级规则
        if (matchedRule == null) {
            matchedRule = findMatchedRule(rules, param.getProvinceCode());
        }

        // 如果没有匹配任何规则，尝试使用默认规则
        if (matchedRule == null) {
            // 查找默认规则
            matchedRule = findDefaultRule(rules);
            if (matchedRule != null) {
                log.info("区域 {} 未找到匹配的运费规则，使用默认规则", param.getProvinceCode());
            }
        }

        // 如果仍没有匹配的规则，返回0
        if (matchedRule == null) {
            return BigDecimal.ZERO;
        }

        if (ChargeTypeEnum.BY_PIECE.getValue().equals(chargeType)) {
            return calculateByQuantity(matchedRule, param.getTotalQuantity());
        } else {
            return calculateByWeight(matchedRule, param.getTotalWeight());
        }
    }

    /**
     * 查找匹配的运费规则
     *
     * @param rules      规则列表
     * @param regionCode 区域编码
     * @return 匹配的规则，未找到则返回null
     */
    private FreightRuleDTO findMatchedRule(List<FreightRuleDTO> rules, String regionCode) {
        if (regionCode == null || rules == null || rules.isEmpty()) {
            return null;
        }

        for (FreightRuleDTO rule : rules) {
            // 跳过默认规则，默认规则在区域编码检查后才使用
            if (Integer.valueOf(1).equals(rule.getIsDefault())) {
                continue;
            }

            if (rule.getRegionCodes() != null) {
                // 区域编码以逗号分隔
                String[] codes = rule.getRegionCodes().split(",");
                for (String code : codes) {
                    if (regionCode.equals(code.trim())) {
                        return rule;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查找默认运费规则
     *
     * @param rules 规则列表
     * @return 默认规则，未找到则返回null
     */
    private FreightRuleDTO findDefaultRule(List<FreightRuleDTO> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        return rules.stream()
                .filter(rule -> Integer.valueOf(1).equals(rule.getIsDefault()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 按件数计费
     *
     * @param rule     运费规则
     * @param quantity 商品数量
     * @return 运费金额(单位：元)
     */
    private BigDecimal calculateByQuantity(FreightRuleDTO rule, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }

        // 使用规则中的实际数据
        int firstItem = rule.getFirstItem();
        BigDecimal firstFee = rule.getFirstFee();
        int continueItem = rule.getContinueItem();
        BigDecimal continueFee = rule.getContinueFee();

        if (firstFee == null || continueFee == null) {
            return BigDecimal.ZERO;
        }

        if (quantity <= firstItem) {
            return firstFee.setScale(2, RoundingMode.CEILING);
        } else {
            // 计算续件费用
            int extraQuantity = quantity - firstItem;
            int extraMultiple = (extraQuantity + continueItem - 1) / continueItem; // 向上取整
            return firstFee.add(continueFee.multiply(BigDecimal.valueOf(extraMultiple))).setScale(2, RoundingMode.CEILING);
        }
    }

    /**
     * 验证和处理默认规则，确保只有一个默认规则
     *
     * @param rules 运费规则列表
     */
    private void validateAndProcessDefaultRules(List<FreightTemplateForm.FreightRuleForm> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        // 统计默认规则数量
        long defaultRuleCount = rules.stream()
                .filter(rule -> Integer.valueOf(1).equals(rule.getIsDefault()))
                .count();

        // 如果有多个默认规则，只保留第一个
        if (defaultRuleCount > 1) {
            log.warn("检测到多个默认规则，只保留第一个");
            boolean foundDefault = false;
            for (FreightTemplateForm.FreightRuleForm rule : rules) {
                if (Integer.valueOf(1).equals(rule.getIsDefault())) {
                    if (foundDefault) {
                        rule.setIsDefault(0);
                    } else {
                        foundDefault = true;
                    }
                }
            }
        }

        // 如果没有默认规则，创建一个默认规则
        if (defaultRuleCount == 0) {
            FreightTemplateForm.FreightRuleForm defaultRule = new FreightTemplateForm.FreightRuleForm();
            defaultRule.setIsDefault(1);
            defaultRule.setRegionCodes("DEFAULT");
            defaultRule.setRegionNames("默认区域");
            defaultRule.setFirstItem(1);
            defaultRule.setFirstFee(BigDecimal.valueOf(1000)); // 10元
            defaultRule.setContinueItem(1);
            defaultRule.setContinueFee(BigDecimal.valueOf(500)); // 5元
            rules.add(defaultRule);
            log.info("创建默认运费规则");
        }
    }

    /**
     * 按重量计费
     *
     * @param rule   运费规则
     * @param weight 商品总重量(单位:克)
     * @return 运费金额(单位：元)
     */
    private BigDecimal calculateByWeight(FreightRuleDTO rule, Integer weight) {
        if (weight == null || weight <= 0) {
            return BigDecimal.ZERO;
        }

        // 使用规则中的实际数据
        Integer firstWeight = rule.getFirstItem();
        BigDecimal firstWeightFee = rule.getFirstFee();
        Integer continueWeight = rule.getContinueItem();
        BigDecimal continueWeightFee = rule.getContinueFee();

        // 如果规则没有数据，使用默认值
        if (firstWeight == null || firstWeightFee == null) {
            firstWeight = 1000; // 首重1000克(1千克)
            firstWeightFee = BigDecimal.valueOf(8); // 8元
        }
        if (continueWeight == null || continueWeightFee == null) {
            continueWeight = 1000; // 续重1000克
            continueWeightFee = BigDecimal.valueOf(5); // 5元
        }

        if (weight <= firstWeight) {
            return firstWeightFee.setScale(2, RoundingMode.CEILING);
        } else {
            // 计算续重费用
            int extraWeight = weight - firstWeight;
            int extraMultiple = (extraWeight + continueWeight - 1) / continueWeight; // 向上取整
            return firstWeightFee.add(continueWeightFee.multiply(BigDecimal.valueOf(extraMultiple))).setScale(2, RoundingMode.CEILING);
        }
    }
}
