package com.deko.mall.pay.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.common.result.PageResult;
import com.deko.mall.common.result.Result;
import com.deko.mall.common.security.util.SecurityUtils;
import com.deko.mall.pay.api.enums.PayChannelEnum;
import com.deko.mall.pay.api.enums.PayStatusEnum;
import com.deko.mall.pay.config.PayResultCode;
import com.deko.mall.pay.entity.PayOfflinePayment;
import com.deko.mall.pay.entity.PayPayment;
import com.deko.mall.pay.entity.PayPaymentChannel;
import com.deko.mall.pay.mapper.PayOfflinePaymentMapper;
import com.deko.mall.pay.mapper.PaymentMapper;
import com.deko.mall.pay.model.vo.OfflinePayDetailVO;
import com.deko.mall.pay.model.vo.OfflinePayPageVO;
import com.deko.mall.pay.service.pay.PaymentChannelService;
import com.deko.mall.pay.service.pay.impl.PayServiceImpl;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin-线下支付审核控制器
 */
@Tag(name = "Admin-线下支付审核")
@RestController
@RequestMapping("/api/v1/pay/offline")
@RequiredArgsConstructor
@Slf4j
public class OfflinePayController {

    private final PayOfflinePaymentMapper offlinePaymentMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentChannelService paymentChannelService;
    private final PayServiceImpl payService;

    @Operation(summary = "线下支付审核分页列表")
    @GetMapping("/page")
    public PageResult<OfflinePayPageVO> getPage(
            @Parameter(description = "支付单号") @RequestParam(required = false) String paymentNo,
            @Parameter(description = "会员ID") @RequestParam(required = false) Long memberId,
            @Parameter(description = "审核状态: 0待审核, 1通过, 2驳回, 不传=全部") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "提交开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "提交结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {

        // 1. 优先查 pay_payment 过滤 memberId（memberId 仅在 pay_payment 表）
        List<String> filteredPaymentNos = null;
        if (memberId != null) {
            filteredPaymentNos = paymentMapper.selectList(
                    new LambdaQueryWrapper<PayPayment>()
                            .eq(PayPayment::getMemberId, memberId)
                            .select(PayPayment::getPaymentNo))
                    .stream().map(PayPayment::getPaymentNo).collect(Collectors.toList());
            if (filteredPaymentNos.isEmpty()) {
                return PageResult.success(new Page<OfflinePayPageVO>(pageNum, pageSize).setRecords(List.of()));
            }
        }

        // 2. 分页查 pay_offline_payment
        LambdaQueryWrapper<PayOfflinePayment> wrapper = new LambdaQueryWrapper<>();
        if (filteredPaymentNos != null) {
            wrapper.in(PayOfflinePayment::getPaymentNo, filteredPaymentNos);
        }
        if (StrUtil.isNotBlank(paymentNo)) {
            wrapper.eq(PayOfflinePayment::getPaymentNo, paymentNo);
        }
        if (auditStatus != null) {
            wrapper.eq(PayOfflinePayment::getAuditStatus, auditStatus);
        }
        if (StrUtil.isNotBlank(startTime)) {
            wrapper.ge(PayOfflinePayment::getCreateTime, startTime);
        }
        if (StrUtil.isNotBlank(endTime)) {
            wrapper.le(PayOfflinePayment::getCreateTime, endTime);
        }
        wrapper.orderByDesc(PayOfflinePayment::getCreateTime);
        Page<PayOfflinePayment> offlinePage = offlinePaymentMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        if (offlinePage.getRecords().isEmpty()) {
            return PageResult.success(new Page<OfflinePayPageVO>(pageNum, pageSize).setRecords(List.of()));
        }

        // 2. 批量查 pay_payment 获取金额和 memberId
        List<String> paymentNos = offlinePage.getRecords().stream()
                .map(PayOfflinePayment::getPaymentNo).collect(Collectors.toList());
        Map<String, PayPayment> paymentMap = paymentMapper.selectList(
                new LambdaQueryWrapper<PayPayment>().in(PayPayment::getPaymentNo, paymentNos))
                .stream().collect(Collectors.toMap(PayPayment::getPaymentNo, p -> p));

        // 3. 组装 VO
        List<OfflinePayPageVO> voList = new ArrayList<>();
        for (PayOfflinePayment off : offlinePage.getRecords()) {
            OfflinePayPageVO vo = new OfflinePayPageVO();
            vo.setId(off.getId());
            vo.setPaymentNo(off.getPaymentNo());
            vo.setVoucherUrls(off.getVoucherUrls());
            vo.setAuditStatus(off.getAuditStatus());
            vo.setAuditRemark(off.getAuditRemark());
            vo.setAuditTime(off.getAuditTime());
            vo.setAuditor(off.getAuditor());
            vo.setResubmitCount(off.getResubmitCount());
            vo.setCreateTime(off.getCreateTime());

            PayPayment pay = paymentMap.get(off.getPaymentNo());
            if (pay != null) {
                vo.setPayAmount(pay.getAmount());
                vo.setMemberId(pay.getMemberId());
            }
            voList.add(vo);
        }

        Page<OfflinePayPageVO> resultPage = new Page<>(pageNum, pageSize, offlinePage.getTotal());
        resultPage.setRecords(voList);
        return PageResult.success(resultPage);
    }

    @Operation(summary = "获取线下支付凭证详情")
    @GetMapping("/{paymentNo}")
    public Result<OfflinePayDetailVO> getDetail(
            @Parameter(description = "支付单号") @PathVariable String paymentNo) {
        PayOfflinePayment offlinePayment = offlinePaymentMapper.selectOne(
                new LambdaQueryWrapper<PayOfflinePayment>()
                        .eq(PayOfflinePayment::getPaymentNo, paymentNo)
                        .orderByDesc(PayOfflinePayment::getCreateTime)
                        .last("LIMIT 1"));
        if (offlinePayment == null) {
            return Result.failed(PayResultCode.OFFLINE_PAY_RECORD_NOT_FOUND);
        }
        OfflinePayDetailVO vo = new OfflinePayDetailVO();
        vo.setPaymentNo(offlinePayment.getPaymentNo());
        vo.setPaymentChannelNo(offlinePayment.getPaymentChannelNo());
        vo.setVoucherUrls(offlinePayment.getVoucherUrls());
        vo.setAuditStatus(offlinePayment.getAuditStatus());
        vo.setAuditRemark(offlinePayment.getAuditRemark());
        vo.setAuditTime(offlinePayment.getAuditTime());
        vo.setAuditor(offlinePayment.getAuditor());
        vo.setResubmitCount(offlinePayment.getResubmitCount());
        return Result.success(vo);
    }

    @Operation(summary = "审核线下支付")
    @PostMapping("/{paymentNo}/audit")
    public Result<Boolean> audit(
            @Parameter(description = "支付单号") @PathVariable String paymentNo,
            @Parameter(description = "是否通过") @RequestParam Boolean approved,
            @Parameter(description = "审核备注") @RequestParam(required = false) String remark) {

        String auditor = SecurityUtils.getUsername();

        // 查询待审核记录
        PayOfflinePayment offlinePayment = offlinePaymentMapper.selectOne(
                new LambdaQueryWrapper<PayOfflinePayment>()
                        .eq(PayOfflinePayment::getPaymentNo, paymentNo)
                        .eq(PayOfflinePayment::getAuditStatus, 0)
                        .orderByDesc(PayOfflinePayment::getCreateTime)
                        .last("LIMIT 1"));
        if (offlinePayment == null) {
            return Result.failed(PayResultCode.OFFLINE_PAY_ALREADY_AUDITED);
        }

        // 查询对应的渠道支付请求
        PayPaymentChannel channel = paymentChannelService.lambdaQuery()
                .eq(PayPaymentChannel::getPaymentChannelNo, offlinePayment.getPaymentChannelNo())
                .eq(PayPaymentChannel::getPaymentChannel, PayChannelEnum.OFFLINE_PAY.getValue())
                .one();
        if (channel == null) {
            return Result.failed(PayResultCode.OFFLINE_PAY_CHANNEL_NOT_FOUND);
        }

        if (approved) {
            // 审核通过：复用 handlePaySuccess 完成支付
            offlinePayment.setAuditStatus(1);
            offlinePayment.setAuditRemark(remark);
            offlinePayment.setAuditTime(LocalDateTime.now());
            offlinePayment.setAuditor(auditor);
            offlinePaymentMapper.updateById(offlinePayment);

            // 调用 handlePaySuccess（完全复用线上支付回调逻辑）
            payService.handlePaySuccess(channel, offlinePayment.getPaymentChannelNo(),
                    "{\"auditor\":\"" + auditor + "\",\"remark\":\"" + (remark != null ? remark : "") + "\"}");
            log.info("线下支付审核通过, paymentNo={}, auditor={}", paymentNo, auditor);
        } else {
            // 审核驳回：回退支付单状态
            offlinePayment.setAuditStatus(2);
            offlinePayment.setAuditRemark(remark);
            offlinePayment.setAuditTime(LocalDateTime.now());
            offlinePayment.setAuditor(auditor);
            offlinePayment.setResubmitCount(
                    offlinePayment.getResubmitCount() != null ? offlinePayment.getResubmitCount() + 1 : 1);
            offlinePaymentMapper.updateById(offlinePayment);

            // 回退渠道支付请求状态
            channel.setStatus(PayStatusEnum.PAY_FAIL.getValue());
            paymentChannelService.updateById(channel);

            // 回退业务支付单状态（允许重新提交）
            payService.resetPaymentToWaitPay(paymentNo);
            log.info("线下支付审核驳回, paymentNo={}, auditor={}, remark={}", paymentNo, auditor, remark);
        }

        return Result.success(true);
    }
}
