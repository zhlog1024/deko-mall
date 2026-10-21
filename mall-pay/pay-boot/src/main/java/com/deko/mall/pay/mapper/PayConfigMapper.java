package com.deko.mall.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.pay.entity.PayConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付配置数据访问层
 */
@Mapper
public interface PayConfigMapper extends BaseMapper<PayConfig> {
}
