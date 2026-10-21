package com.deko.mall.pay.service.pay;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pay.entity.PayConfig;
import com.deko.mall.pay.model.form.PayConfigForm;
import com.deko.mall.pay.model.query.PayConfigQuery;
import com.deko.mall.pay.model.vo.PayChannelVO;
import com.deko.mall.pay.model.vo.PayConfigVO;

import java.util.List;

/**
 * 支付配置服务接口
 */
public interface PayConfigService extends IService<PayConfig> {

    /**
     * 获取所有启用的支付配置
     */
    List<PayConfig> getEnabledConfigs();

    List<PayConfig> getAllConfigs();

    /**
     * 获取指定支付渠道的所有启用配置
     *
     * @param payChannel 支付渠道
     * @return 该渠道的所有启用配置
     */
    List<PayConfig> getEnabledConfigsByChannel(String payChannel);

    /**
     * 分页获取支付配置
     */
    Page<PayConfigVO> getPayConfigPage(PayConfigQuery queryParams);

    /**
     * 获取支付配置详情
     */
    PayConfigVO getPayConfigDetail(Long id);

    /**
     * 新增支付配置
     */
    PayConfig addPayConfig(PayConfigForm form);

    /**
     * 修改支付配置
     */
    PayConfig updatePayConfig(Long id, PayConfigForm form);

    /**
     * 删除支付配置
     */
    boolean deletePayConfigs(List<Long> ids);

    /**
     * 获取支付渠道列表
     */
    List<PayChannelVO> getPayChannels();
}
