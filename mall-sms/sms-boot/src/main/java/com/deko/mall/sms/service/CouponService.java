package com.deko.mall.sms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.sms.model.entity.SmsCoupon;
import com.deko.mall.sms.model.form.CouponForm;
import com.deko.mall.sms.model.query.CouponPageQuery;
import com.deko.mall.sms.model.vo.CouponPageVO;

/**
 * 优惠券业务接口
 */
public interface CouponService extends IService<SmsCoupon> {

    /**
     * 优惠券分页列表
     */
    Page<CouponPageVO> getCouponPage(CouponPageQuery queryParams);

    /**
     * 新增优惠券
     */
    boolean saveCoupon(CouponForm couponForm);

    /**
     * 修改优惠券
     *
     * @param couponId   优惠券ID
     * @param couponForm 优惠券表单
     */
    boolean updateCoupon(Long couponId, CouponForm couponForm);

    /**
     * 删除优惠券
     *
     * @param ids 优惠券ID，多个以英文逗号(,)分割
     */
    boolean deleteCoupons(String ids);

    /**
     * 优惠券表单数据
     */
    CouponForm getCouponFormData(Long couponId);
}
