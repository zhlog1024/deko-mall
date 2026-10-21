package com.deko.mall.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.sms.model.entity.SmsCoupon;
import com.deko.mall.sms.model.query.CouponPageQuery;
import com.deko.mall.sms.model.vo.CouponPageVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface CouponMapper extends BaseMapper<SmsCoupon> {

    List<SmsCoupon> getCouponPage(Page<CouponPageVO> page, CouponPageQuery queryParams);
}




