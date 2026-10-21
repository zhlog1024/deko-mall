package com.deko.mall.pay.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pay.converter.PayConfigConverter;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.mapper.PayConfigMapper;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.model.form.PayConfigForm;
import com.deko.mall.pay.model.query.PayConfigQuery;
import com.deko.mall.pay.model.vo.PayChannelVO;
import com.deko.mall.pay.model.vo.PayConfigVO;
import com.deko.mall.pay.service.pay.PayConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.deko.mall.pay.config.PayResultCode;

/**
 * 支付配置服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PayConfigServiceImpl extends ServiceImpl<PayConfigMapper, PayConfig> implements PayConfigService {

    private final PayConfigConverter payConfigConverter;

    @Override
    public List<PayConfig> getEnabledConfigs() {
        return this.list(
                new LambdaQueryWrapper<PayConfig>()
                        .eq(PayConfig::getEnabled, true)
        );
    }


    @Override
    public List<PayConfig> getAllConfigs() {
        return this.list(
                new LambdaQueryWrapper<PayConfig>()
        );
    }

    @Override
    public List<PayConfig> getEnabledConfigsByChannel(String payChannel) {
        return this.list(
                new LambdaQueryWrapper<PayConfig>()
                        .eq(PayConfig::getPayChannel, payChannel)
                        .eq(PayConfig::getEnabled, true)
        );
    }

    @Override
    public Page<PayConfigVO> getPayConfigPage(PayConfigQuery queryParams) {
        Page<PayConfig> payConfigPage = this.page(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                new LambdaQueryWrapper<PayConfig>()
                        .eq(StrUtil.isNotBlank(queryParams.getPayChannel()), PayConfig::getPayChannel, queryParams.getPayChannel())
                        .eq(queryParams.getEnabled() != null, PayConfig::getEnabled, queryParams.getEnabled())
        );
        Page<PayConfigVO> voPage = payConfigConverter.toVoPage(payConfigPage);
        return voPage;
    }

    @Override
    public PayConfigVO getPayConfigDetail(Long id) {
        PayConfig entity = this.getById(id);
        if (entity != null) {
            // 解析扩展配置JSON
            return payConfigConverter.entity2Vo(entity);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayConfig addPayConfig(PayConfigForm form) {
        // 允许同一渠道有多个配置
        PayConfig entity = payConfigConverter.form2Entity(form);
        entity.setChannelName(PayChannelEnum.getByValue(entity.getPayChannel()).getLabel());
        this.save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayConfig updatePayConfig(Long id, PayConfigForm form) {
        // 检查配置是否存在
        PayConfig existConfig = this.getById(id);
        if (existConfig == null) {
            throw new BizException(PayResultCode.PAY_CONFIG_NOT_FOUND);
        }

        form.setId(id);
        PayConfig entity = payConfigConverter.form2Entity(form);
        entity.setChannelName(PayChannelEnum.getByValue(entity.getPayChannel()).getLabel());
        this.updateById(entity);
        return existConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePayConfigs(List<Long> ids) {
        // 检查是否存在启用的配置
        long enabledCount = this.count(
                new LambdaQueryWrapper<PayConfig>()
                        .in(PayConfig::getId, ids)
                        .eq(PayConfig::getEnabled, true)
        );
        if (enabledCount > 0) {
            throw new BizException(PayResultCode.PAY_CONFIG_ACTIVE_EXISTS);
        }
        return this.removeBatchByIds(ids);
    }

    @Override
    public List<PayChannelVO> getPayChannels() {
        return Arrays.stream(PayChannelEnum.values())
                .map(channel -> {
                    PayChannelVO vo = new PayChannelVO();
                    vo.setChannelCode(channel.getValue());
                    vo.setChannelName(channel.getLabel());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}