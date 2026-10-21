package com.deko.mall.ums.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.ums.dto.MemberAddressDTO;
import com.deko.mall.ums.dto.MemberAuthDTO;
import com.deko.mall.ums.dto.MemberInfoDTO;
import com.deko.mall.ums.dto.MemberRegisterDTO;
import com.deko.mall.ums.model.entity.UmsMember;
import com.deko.mall.ums.model.form.UmsMemberQuery;
import com.deko.mall.ums.model.vo.MemberVO;

import java.util.List;

/**
 * 会员业务接口
 */
public interface UmsMemberService extends IService<UmsMember> {

    IPage<UmsMember> list(Page<UmsMember> page, String nickname);

    /**
     * 分页查询会员列表（包含推荐人信息）
     *
     * @param page  分页参数
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<MemberVO> listMembers(Page<MemberVO> page, UmsMemberQuery query);


    /**
     * 根据 openid 获取会员认证信息
     */
    MemberAuthDTO getMemberByOpenid(String openid);

    /**
     * 根据手机号获取会员认证信息
     */
    MemberAuthDTO getMemberByMobile(String mobile);

    Boolean getMemberByMobileRegistered(String mobile);

    /**
     * 新增会员
     */
    Long addMember(MemberRegisterDTO member);

    /**
     * 获取登录会员信息
     */
    MemberVO getCurrMemberInfo();

    /**
     * 获取会员地址列表
     */
    List<MemberAddressDTO> listMemberAddress(Long memberId);


    Boolean delMember();

    Boolean updateMember(MemberRegisterDTO member);

    List<UmsMember> getMemberInfoByUserId(List<Long> userIds);

    Long registerMember(MemberRegisterDTO member);

    Boolean checkUserId(Long userId);

    UmsMember getUserInfoByUserId(Long userId);

    MemberInfoDTO getInfoByUserId(Long userId);

    MemberAuthDTO getWxUserInfo(String code, String encryptedData, String iv);

    String getWxOpenidByCode(String code);
}
