package com.deko.mall.ums.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.constant.RedisConstants;
import com.deko.mall.common.constant.SystemConstants;
import com.deko.mall.common.result.ResultCode;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.ums.config.UmsResultCode;
import com.deko.mall.ums.convert.AddressConvert;
import com.deko.mall.ums.convert.MemberAssetConvert;
import com.deko.mall.ums.convert.MemberConvert;
import com.deko.mall.ums.dto.*;
import com.deko.mall.ums.enums.UserType;
import com.deko.mall.ums.mapper.UmsMemberMapper;
import com.deko.mall.ums.model.entity.UmsAddress;
import com.deko.mall.ums.model.entity.UmsMember;
import com.deko.mall.ums.model.form.UmsMemberQuery;
import com.deko.mall.ums.model.vo.MemberVO;
import com.deko.mall.oms.api.OrderFeignClient;
import com.deko.mall.ums.service.UmsAddressService;
import com.deko.mall.ums.service.UmsMemberAssetsService;
import com.deko.mall.ums.service.UmsMemberService;
import com.deko.mall.ums.service.UmsMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 会员业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements UmsMemberService {

    private final WxMaService wxMaService;
    private final MemberAssetConvert memberAssetsConvert;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberConvert memberConvert;
    @Autowired
    private UmsMemberMapper umsMemberMapper;
    @Autowired
    private AddressConvert addressConvert;
    @Autowired
    private UmsAddressService addressService;
    @Autowired
    private UmsMessageService umsMessageService;
    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private UmsMemberAssetsService umsMemberAssetsService;


    @Override
    public IPage<UmsMember> list(Page<UmsMember> page, String nickname) {
        List<UmsMember> list = this.baseMapper.list(page, nickname);
        page.setRecords(list);
        return page;
    }

    @Override
    public IPage<MemberVO> listMembers(Page<MemberVO> page, UmsMemberQuery query) {
        return this.baseMapper.selectMemberPage(page, query);
    }

    /**
     * 根据 openid 获取会员认证信息
     *
     * @param openid 微信唯一身份标识
     */
    @Override
    public MemberAuthDTO getMemberByOpenid(String openid) {
        UmsMember entity = this.getOne(new LambdaQueryWrapper<UmsMember>()
                .eq(UmsMember::getOpenid, openid)
                .select(UmsMember::getId,
                        UmsMember::getOpenid,
                        UmsMember::getStatus
                )
        );

        if (entity == null) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return memberConvert.entity2OpenidAuthDTO(entity);
    }

    /**
     * 根据手机号获取会员认证信息
     */
    @Override
    public MemberAuthDTO getMemberByMobile(String mobile) {
        UmsMember entity = this.getOne(new LambdaQueryWrapper<UmsMember>()
                .eq(UmsMember::getMobile, mobile)
                .eq(UmsMember::getDeleted, 0)
                .select(UmsMember::getId,
                        UmsMember::getMobile,
                        UmsMember::getStatus,
                        UmsMember::getPassword
                )
        );

        if (entity == null) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        MemberAuthDTO memberAuthDTO = memberConvert.entity2MobileAuthDTO(entity);
        return memberAuthDTO;
    }

    @Override
    public Boolean getMemberByMobileRegistered(String mobile) {
        UmsMember entity = this.getOne(new LambdaQueryWrapper<UmsMember>()
                .eq(UmsMember::getMobile, mobile)
                .select(UmsMember::getId,
                        UmsMember::getMobile,
                        UmsMember::getStatus,
                        UmsMember::getPassword
                )
        );
        return entity != null;
    }

    /**
     * 新增会员
     */
    @Override
    public Long
    addMember(MemberRegisterDTO memberRegisterDTO) {
        UmsMember umsMember = memberConvert.dto2Entity(memberRegisterDTO);
        // 设置国家区号，默认 +86
        if (memberRegisterDTO.getCountryCode() == null || memberRegisterDTO.getCountryCode().isBlank()) {
            umsMember.setCountryCode("+86");
        }
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 200, 4, 32, 16);
        umsMember.setPassword(argon2PasswordEncoder.encode(SystemConstants.DEFAULT_PASSWORD));
        if (StringUtils.isBlank(memberRegisterDTO.getPassword())) {
            umsMember.setPassword(argon2PasswordEncoder.encode(SystemConstants.DEFAULT_PASSWORD));
        } else {
            umsMember.setPassword(argon2PasswordEncoder.encode(memberRegisterDTO.getPassword()));
        }
        umsMember.setRoleCode(UserType.NORMAL.getCode());
        // 默认头像为占位地址，部署时替换为实际 CDN 地址。
        if (StringUtils.isBlank(umsMember.getAvatarUrl())) {
            umsMember.setAvatarUrl("https://example-bucket.oss-cn-hangzhou.aliyuncs.com/static/app/warm/common/user.png");
        }
        // generate initial random payment discount (10 - 1000 cents = 0.10 - 10.00 CNY)
        try {
            long discount = ThreadLocalRandom.current().nextLong(10, 1000 + 1);
            umsMember.setPaymentDiscountAmount(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100)));
        } catch (Exception ignore) {
        }
        boolean result = this.save(umsMember);
        Assert.isTrue(result, "新增会员失败");
        umsMemberAssetsService.initAllMemberAssets(umsMember.getId());
        return umsMember.getId();
    }

    /**
     * 获取登录会员信息
     */
    @Override
    public MemberVO getCurrMemberInfo() {
        Long memberId = SecurityUtils.getMemberId();
        MemberVO memberVO = umsMemberMapper.getCurrentMemberInfo(memberId);
        if (memberVO == null) {
            return null;
        }
        // 会员统一展示为普通用户。
        memberVO.setMemberType("普通用户");
        // 设置资产列表
        List<MemberAssetsVO> memberAssetsVOS = memberAssetsConvert.dtoList2VoList(umsMemberAssetsService.getMemberAssets(memberId));
        memberVO.setAssets(memberAssetsVOS);
        // 未读消息数
        memberVO.setMessageTotal(umsMessageService.countUnread(memberId));

        // 订单状态计数（导航栏角标用）
        try {
            var orderResult = orderFeignClient.getOrdersByMemberId(memberId);
            if (com.deko.mall.common.result.Result.isSuccess(orderResult) && orderResult.getData() != null) {
                java.util.LinkedHashMap<String, Integer> statusMap = new java.util.LinkedHashMap<>();
                for (var o : orderResult.getData()) {
                    String key = "status_" + o.getStatus();
                    statusMap.merge(key, 1, Integer::sum);
                }
                memberVO.setOrderStatusCounts(statusMap);
            }
        } catch (Exception e) {
            log.warn("获取订单状态计数失败", e);
        }

        return memberVO;
    }

    /**
     * 获取会员地址
     */
    @Override
    public List<MemberAddressDTO> listMemberAddress(Long memberId) {

        List<UmsAddress> entities = addressService.list(
                new LambdaQueryWrapper<UmsAddress>()
                        .eq(UmsAddress::getMemberId, memberId)
        );

        List<MemberAddressDTO> list = addressConvert.entity2Dto(entities);
        return list;
    }

    @Override
    public Boolean delMember() {
        Long memberId = SecurityUtils.getMemberId();
        // 根据手机号 更新 用户deleted状态为1
        return this.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, memberId)
                .eq(UmsMember::getDeleted, 0)
                .set(UmsMember::getDeleted, 1)
                .set(UmsMember::getUpdateTime, LocalDateTime.now())
        );
    }

    @Override
    public Boolean updateMember(MemberRegisterDTO member) {
        Long memberId = SecurityUtils.getMemberId();

        // 根据member id 查出用户 ，判断用户是否存在
        UmsMember entity = this.getOne(new LambdaQueryWrapper<UmsMember>()
                .eq(UmsMember::getId, memberId)
                .eq(UmsMember::getDeleted, 0)
        );
        Assert.notNull(entity, "用户不存在");
        if (StringUtils.isNotBlank(member.getPassword())) {
            Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 200, 4, 32, 16);
            entity.setPassword(argon2PasswordEncoder.encode(member.getPassword()));
        }
        if (StringUtils.isNotBlank(member.getNickName())) {
            entity.setNickName(member.getNickName());
        }
        if (StringUtils.isNotBlank(member.getAvatarUrl())) {
            entity.setAvatarUrl(member.getAvatarUrl());
        }
        if (member.getGender() != null) {
            entity.setGender(member.getGender());
        }
        if (member.getBirthday() != null) {
            entity.setBirthday(member.getBirthday());
        }
        return this.updateById(entity);
    }

    @Override
    public Long registerMember(MemberRegisterDTO member) {
        Assert.notBlank(member.getInvitationCode(), "请输入验证码");
        String codeKey = RedisConstants.LOGIN_SMS_CODE_PREFIX + member.getMobile();

        String cacheCode = (String) redisTemplate.opsForValue().get(codeKey);
        Assert.isTrue(StrUtil.equals(member.getInvitationCode(), cacheCode), "验证码错误");

        return this.addMember(member);
    }

    @Override
    public Boolean checkUserId(Long userId) {
        UmsMember umsMember = umsMemberMapper.selectOne(new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getId, userId));
        if (umsMember != null) {
            return true;
        }
        return false;
    }

    @Override
    public UmsMember getUserInfoByUserId(Long userId) {
        UmsMember umsMember = umsMemberMapper.selectOne(new LambdaQueryWrapper<UmsMember>().eq(UmsMember::getId, userId));
        return umsMember;
    }

    @Override
    public MemberInfoDTO getInfoByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        UmsMember user = this.getUserInfoByUserId(userId);
        if (user == null) {
            log.warn("会员不存在: userId={}", userId);
            return null;
        }
        MemberInfoDTO result = memberConvert.entity2InfoDto(user);
        result.setUserId(user.getId());
        return result;
    }

    @Override
    public MemberAuthDTO getWxUserInfo(String code, String encryptedData, String iv) {
        WxMaJscode2SessionResult sessionInfo;
        try {
            sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            String sessionKey = sessionInfo.getSessionKey();
            log.info("sessionKey length: " + sessionKey.length());
            WxMaPhoneNumberInfo wxMaPhoneNumberInfo = wxMaService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
            String phoneNumber = wxMaPhoneNumberInfo.getPhoneNumber();

            return MemberAuthDTO.builder()
                    .username(phoneNumber)
                    .mobile(phoneNumber).build();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getWxOpenidByCode(String code) {
        WxMaJscode2SessionResult sessionInfo;
        try {
            sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            return sessionInfo.getOpenid();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UmsMember> getMemberInfoByUserId(List<Long> userIds) {
        return this.list(new LambdaQueryWrapper<UmsMember>().in(UmsMember::getId, userIds));
    }


}
