package com.deko.mall.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.ums.config.UmsResultCode;
import com.deko.mall.ums.convert.MemberAssetConvert;
import com.deko.mall.ums.dto.AssetOperateRequest;
import com.deko.mall.ums.dto.MemberAssetsDTO;
import com.deko.mall.ums.enums.AssetCodeEnum;
import com.deko.mall.ums.enums.AssetOperateSceneEnum;
import com.deko.mall.ums.mapper.UmsMemberAssetsFlowMapper;
import com.deko.mall.ums.mapper.UmsMemberAssetsMapper;
import com.deko.mall.ums.mapper.UmsMemberMapper;
import com.deko.mall.ums.model.domain.UmsMemberAssets;
import com.deko.mall.ums.model.domain.UmsMemberAssetsFlow;
import com.deko.mall.ums.model.entity.UmsMember;
import com.deko.mall.ums.service.UmsMemberAssetsService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 会员资产服务实现
 * <p>
 * 设计原则：
 * - operate(): 统一入口，处理所有操作场景
 * - 私有方法: 场景分发和核心逻辑
 */
@Slf4j
@Service
public class UmsMemberAssetsServiceImpl extends ServiceImpl<UmsMemberAssetsMapper, UmsMemberAssets>
        implements UmsMemberAssetsService {

    // 锁 Key 常量
    private static final String ASSET_LOCK_KEY_PATTERN = "asset:%s:%d:%s";
    private static final String ASSET_TRANSFER_LOCK_KEY_PATTERN = "asset:transfer:%s,%s:%s";

    @Autowired
    private UmsMemberAssetsMapper umsMemberAssetsMapper;

    @Autowired
    private MemberAssetConvert memberAssetConvert;

    @Autowired
    private UmsMemberAssetsFlowMapper umsMemberAssetsFlowMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    // 统一操作入口

    /**
     * 通用资产操作
     * 统一处理：订单、转账、内部调整等场景
     */
    @Override
    public boolean operate(AssetOperateRequest request) {
        AssetOperateSceneEnum operateType = request.getOperateType();
        if (operateType == null) {
            throw new BizException(UmsResultCode.ASSET_OPERATE_TYPE_REQUIRED);
        }

        return switch (operateType) {
            case ORDER_CREATE_DEDUCT -> doOrderCreateDeduct(request);
            case ORDER_CANCEL_REFUND -> doOrderCancelRefund(request);
            case ORDER_REFUND_REFUND -> doOrderRefundRefund(request);
            case PROFIT_GRANT -> doProfitGrant(request);
            case TRANSFER_OUT -> doTransferOut(request);
            case TRANSFER_IN -> doTransferIn(request);
            case ADJUST_IN -> doAdjustIn(request);
            case ADJUST_OUT -> doAdjustOut(request);
        };
    }

    // 订单场景处理

    private boolean doOrderCreateDeduct(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.of(request.getAssetCode()),
                request.getNum(),
                AssetOperateSceneEnum.ORDER_CREATE_DEDUCT,
                request.getBizNo(),
                "订单下单扣减"
        );
        return true;
    }

    private boolean doOrderCancelRefund(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.of(request.getAssetCode()),
                request.getNum(),
                AssetOperateSceneEnum.ORDER_CANCEL_REFUND,
                request.getBizNo(),
                "订单取消退回"
        );
        return true;
    }

    private boolean doOrderRefundRefund(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.of(request.getAssetCode()),
                request.getNum(),
                AssetOperateSceneEnum.ORDER_REFUND_REFUND,
                request.getBizNo(),
                request.getRemark() != null ? request.getRemark() : "订单退款退回"
        );
        return true;
    }

    // 收益发放场景处理

    private boolean doProfitGrant(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.of(request.getAssetCode()),
                request.getNum(),
                AssetOperateSceneEnum.PROFIT_GRANT,
                request.getBizNo(),
                request.getRemark()
        );
        return true;
    }

    // 转账场景处理

    /**
     * 转账转出
     */
    private boolean doTransferOut(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.POINT,
                request.getNum(),
                AssetOperateSceneEnum.TRANSFER_OUT,
                null,
                "转账支出"
        );
        return true;
    }

    /**
     * 转账转入
     */
    private boolean doTransferIn(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.POINT,
                request.getNum(),
                AssetOperateSceneEnum.TRANSFER_IN,
                null,
                "转账收入"
        );
        return true;
    }

    // 便捷方法

    /**
     * 积分转账
     */
    @Override
    public boolean transferAssets(Long memberId, String receiverMobile, BigDecimal num, String remark) {
        // 1. 验证接收方账户
        UmsMember toMember = validateAndGetToMemberId(receiverMobile);
        UmsMember fromMember = selectMemberIdByUserId(memberId);

        // 生成转账单号（转出和转入共用同一个单号）
        String reqNo = "TF" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));

        // 2. 加锁
        String lockKey = buildTransferLockKey(memberId, toMember.getId().toString(), AssetCodeEnum.POINT);
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 300, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(lockAcquired)) {
            throw new RuntimeException("转账操作冲突，请稍后重试");
        }

        try {
            return transactionTemplate.execute(status -> {
                try {
                    // 3. 查询转账前双方可用金额
                    UmsMemberAssets fromAssetBefore = umsMemberAssetsMapper.selectByMemberAndCode(
                            memberId, AssetCodeEnum.POINT.getCode());
                    UmsMemberAssets toAssetBefore = umsMemberAssetsMapper.selectByMemberAndCode(
                            toMember.getId(), AssetCodeEnum.POINT.getCode());

                    if (fromAssetBefore == null ||
                            fromAssetBefore.getAvailableNum().compareTo(num) < 0) {
                        throw new BizException(UmsResultCode.TRANSFER_POINTS_INSUFFICIENT);
                    }
                    if (toAssetBefore == null) {
                        toAssetBefore = initMemberAssets(toMember.getId(), AssetCodeEnum.POINT);
                    }

                    // 4. 执行转出（内部会自动记录流水）
                    executeAssetOperation(
                            memberId, AssetCodeEnum.POINT, num,
                            AssetOperateSceneEnum.TRANSFER_OUT, reqNo, "转出给：" + toMember.getMobile());

                    // 5. 执行转入（内部会自动记录流水）
                    executeAssetOperation(
                            toMember.getId(), AssetCodeEnum.POINT, num,
                            AssetOperateSceneEnum.TRANSFER_IN, reqNo, "来自：" + fromMember.getMobile() + "的转账");

                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("转账事务执行失败", e);
                    throw new RuntimeException(e.getMessage());
                }
            });
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    // 内部调整场景处理

    /**
     * 系统增加
     */
    private boolean doAdjustIn(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.of(request.getAssetCode()),
                request.getNum(),
                AssetOperateSceneEnum.ADJUST_IN,
                request.getBizNo(),
                request.getAdjustReason() != null ? request.getAdjustReason() : "系统增加"
        );
        return true;
    }

    /**
     * 系统扣减
     */
    private boolean doAdjustOut(AssetOperateRequest request) {
        validatePositiveAmount(request.getNum());
        executeAssetOperation(
                request.getMemberId(),
                AssetCodeEnum.of(request.getAssetCode()),
                request.getNum().negate(),  // 扣减使用负数
                AssetOperateSceneEnum.ADJUST_OUT,
                request.getBizNo(),
                request.getAdjustReason() != null ? request.getAdjustReason() : "系统扣减"
        );
        return true;
    }

    // 核心执行方法

    /**
     * 执行资产操作（带分布式锁和事务）
     */
    private UmsMemberAssets executeAssetOperation(Long memberId, AssetCodeEnum assetCode, BigDecimal amount,
                                                  AssetOperateSceneEnum operateType, String bizNo, String remark) {
        // 资产类型约束校验
        validateAssetOperation(assetCode, operateType);

        // 分布式锁键
        String lockKey = buildAssetLockKey("adjust", memberId, assetCode);

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 300, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(lockAcquired)) {
            throw new RuntimeException("资产操作冲突，请稍后重试");
        }

        try {
            return transactionTemplate.execute(status -> {
                try {
                    return doAdjustAssets(memberId, assetCode, amount, operateType, bizNo, remark);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("资产调整事务执行失败", e);
                    throw new RuntimeException(e.getMessage());
                }
            });
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 实际资产调整逻辑
     */
    private UmsMemberAssets doAdjustAssets(Long memberId, AssetCodeEnum assetCode, BigDecimal amount,
                                           AssetOperateSceneEnum operateType, String bizNo, String remark) {
        UmsMemberAssets assets = umsMemberAssetsMapper.selectByMemberAndCode(memberId, assetCode.getCode());

        // 初始化资产记录（如果不存在）
        if (assets == null) {
            assets = initMemberAssets(memberId, assetCode);
        }

        // 记录变更前可用金额
        BigDecimal beforeAvailableNum = assets.getAvailableNum();

        // 执行资产变更
        updateAssetValues(assets, amount, operateType);
        umsMemberAssetsMapper.updateById(assets);

        // 记录变更后可用金额
        BigDecimal afterAvailableNum = assets.getAvailableNum();

        // 记录资产流水
        createAssetFlowRecord(memberId, amount, operateType, bizNo, remark, assets.getId(), beforeAvailableNum, afterAvailableNum);
        return assets;
    }

    /**
     * 资产类型约束校验
     */
    private void validateAssetOperation(AssetCodeEnum assetCode, AssetOperateSceneEnum operateType) {
        // 转账仅支持积分
        if (operateType.isTransfer() && assetCode != AssetCodeEnum.POINT) {
            throw new BizException(UmsResultCode.ONLY_POINTS_SUPPORT_TRANSFER);
        }
    }

    /**
     * 统一资产变更逻辑（基于 AssetOperateSceneEnum）
     */
    private void updateAssetValues(UmsMemberAssets assets, BigDecimal amount, AssetOperateSceneEnum operateType) {
        BigDecimal absAmount = amount.abs();

        switch (operateType) {
            // 扣减：可用-
            case ORDER_CREATE_DEDUCT:
            case TRANSFER_OUT:
            case ADJUST_OUT:
                if (assets.getAvailableNum().compareTo(absAmount) < 0) {
                    throw new BizException(UmsResultCode.ASSET_INSUFFICIENT, assets.getAssetCode());
                }
                assets.setAvailableNum(assets.getAvailableNum().subtract(absAmount));
                break;

            // 退回/返还：可用+
            case ORDER_CANCEL_REFUND:
            case ORDER_REFUND_REFUND:
            case PROFIT_GRANT:
            case TRANSFER_IN:
            case ADJUST_IN:
                assets.setAvailableNum(assets.getAvailableNum().add(absAmount));
                break;

            default:
                throw new BizException(UmsResultCode.ASSET_OPERATE_TYPE_UNSUPPORTED, operateType.getSceneDescription());
        }
    }

    /**
     * 创建资产流水
     */
    private void createAssetFlowRecord(Long memberId, BigDecimal amount,
                                       AssetOperateSceneEnum operateSceneType, String bizNo, String remark,
                                       Long assetId, BigDecimal beforeAvailableNum, BigDecimal afterAvailableNum) {
        if (!operateSceneType.shouldRecordFlow()) {
            return;
        }

        BigDecimal num = calculateAvailableNum(operateSceneType, amount);

        AssetFlowBuilder.create()
                .memberId(memberId)
                .assetId(assetId)
                .num(num)
                .beforeAvailable(beforeAvailableNum)
                .afterAvailable(afterAvailableNum)
                .type(operateSceneType.getOperateType())
                .sceneCode(operateSceneType.getSceneCode())
                .sceneDescription(operateSceneType.getSceneDescription())
                .reqNo(bizNo)
                .remark(buildRemark(operateSceneType, remark, amount))
                .buildAndSave(umsMemberAssetsFlowMapper);
    }

    /**
     * 计算可用余额变动值（用于流水记录）
     *
     * @return 变动值（正数=增加，负数=减少，0=不变）
     */
    private BigDecimal calculateAvailableNum(AssetOperateSceneEnum operateType, BigDecimal amount) {
        BigDecimal absAmount = amount.abs();

        return switch (operateType) {
            // 收入/增加/退回：可用+
            case PROFIT_GRANT, TRANSFER_IN, ADJUST_IN,
                 ORDER_CANCEL_REFUND, ORDER_REFUND_REFUND -> absAmount;
            // 扣减：可用-
            case TRANSFER_OUT, ADJUST_OUT,
                 ORDER_CREATE_DEDUCT -> absAmount.negate();
        };
    }

    /**
     * 构建流水备注
     */
    private String buildRemark(AssetOperateSceneEnum operateType, String baseRemark, BigDecimal amount) {
        return baseRemark;
    }

    // 查询方法

    @Override
    public MemberAssetsDTO getMemberAsset(Long memberId, String assetCode) {
        if (StringUtil.isBlank(assetCode)) {
            throw new IllegalArgumentException("资产编码不能为空");
        }
        UmsMemberAssets assets = umsMemberAssetsMapper.selectOne(
                new LambdaQueryWrapper<UmsMemberAssets>()
                        .eq(UmsMemberAssets::getMemberId, memberId)
                        .eq(UmsMemberAssets::getAssetCode, assetCode)
                        .eq(UmsMemberAssets::getStatus, 0)
        );

        if (assets == null) {
            throw new IllegalArgumentException("未找到会员[" + memberId + "]的[" + assetCode + "]资产记录");
        }

        return memberAssetConvert.dto2Entity(assets);
    }

    @Override
    public List<MemberAssetsDTO> getMemberAssets(Long memberId) {
        List<UmsMemberAssets> assetsList = umsMemberAssetsMapper.selectList(
                new LambdaQueryWrapper<UmsMemberAssets>()
                        .eq(UmsMemberAssets::getMemberId, memberId)
                        .eq(UmsMemberAssets::getStatus, 0)
        );
        if (CollectionUtils.isEmpty(assetsList)) {
            throw new IllegalArgumentException("未找到会员[" + memberId + "]的资产记录");
        }

        return memberAssetConvert.dto2EntityList(assetsList);
    }

    @Override
    public Page<UmsMemberAssetsFlow> flowAsset(Long memberId, Long assetId, Page<UmsMemberAssetsFlow> objectPage) {
        LambdaQueryWrapper<UmsMemberAssetsFlow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsMemberAssetsFlow::getMemberId, memberId)
                .eq(assetId != null, UmsMemberAssetsFlow::getAssetId, assetId)
                .orderByDesc(UmsMemberAssetsFlow::getId);
        return umsMemberAssetsFlowMapper.selectPage(objectPage, queryWrapper);
    }

    @Override
    public Page<UmsMemberAssetsFlow> flowAssetByAssetCode(Long memberId, String assetCode, String sceneCode, String reqNo, Page<UmsMemberAssetsFlow> objectPage) {
        UmsMemberAssets one = umsMemberAssetsMapper.selectOne(
                new LambdaQueryWrapper<UmsMemberAssets>()
                        .eq(UmsMemberAssets::getMemberId, memberId)
                        .eq(UmsMemberAssets::getAssetCode, assetCode)
                        .eq(UmsMemberAssets::getStatus, 0)
        );
        if (one == null) {
            return objectPage;
        }
        LambdaQueryWrapper<UmsMemberAssetsFlow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsMemberAssetsFlow::getMemberId, memberId)
                .eq(UmsMemberAssetsFlow::getAssetId, one.getId())
                .eq(StringUtil.isNotBlank(sceneCode), UmsMemberAssetsFlow::getSceneCode, sceneCode)
                .eq(StringUtil.isNotBlank(reqNo), UmsMemberAssetsFlow::getReqNo, reqNo)
                .orderByDesc(UmsMemberAssetsFlow::getId);

        return umsMemberAssetsFlowMapper.selectPage(objectPage, queryWrapper);
    }

    @Override
    public UmsMemberAssetsFlow getOrderLockNum(Long memberId, String orderNo) {
        UmsMemberAssetsFlow one = umsMemberAssetsFlowMapper.selectOne(
                new LambdaQueryWrapper<UmsMemberAssetsFlow>()
                        .eq(UmsMemberAssetsFlow::getMemberId, memberId)
                        .eq(UmsMemberAssetsFlow::getReqNo, orderNo)
                        .eq(UmsMemberAssetsFlow::getType, AssetOperateSceneEnum.ORDER_CREATE_DEDUCT.name())
        );
        if (one == null) {
            throw new RuntimeException("未找到会员[" + memberId + "]的订单[" + orderNo + "]的资产记录");
        }
        return one;
    }

    @Override
    public List<Long> getNeedInitUser() {
        return umsMemberAssetsMapper.getNeedInitUser();
    }

    @Override
    public List<UmsMemberAssets> getMemberInfoByUserId(List<Long> userIds) {
        List<UmsMemberAssets> assetsList = umsMemberAssetsMapper.selectList(
                new LambdaQueryWrapper<UmsMemberAssets>()
                        .in(UmsMemberAssets::getMemberId, userIds)
                        .eq(UmsMemberAssets::getStatus, 0)
        );
        return assetsList;
    }

    // 初始化方法

    @Override
    public UmsMemberAssets initMemberAssets(Long memberId, AssetCodeEnum assetCode) {
        UmsMemberAssets assets = new UmsMemberAssets();
        assets.setMemberId(memberId);
        assets.setAssetCode(assetCode.getCode());
        assets.setAvailableNum(BigDecimal.ZERO);
        umsMemberAssetsMapper.insert(assets);
        return assets;
    }

    @Override
    public void initAllMemberAssets(Long memberId) {
        for (AssetCodeEnum value : AssetCodeEnum.values()) {
            this.initMemberAssets(memberId, value);
        }
    }

    // 私有辅助方法

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(UmsResultCode.AMOUNT_MUST_BE_POSITIVE);
        }
    }

    private UmsMember validateAndGetToMemberId(String account) {
        UmsMember member = umsMemberMapper.selectMemberIdByMobile(account);
        if (member == null) {
            throw new IllegalArgumentException("无效的接收方账户: " + account);
        }
        return member;
    }

    private UmsMember selectMemberIdByUserId(Long userId) {
        UmsMember member = umsMemberMapper.selectMemberIdByUserId(userId);
        if (member == null) {
            throw new IllegalArgumentException("无效的会员账户: " + userId);
        }
        return member;
    }

    private String buildAssetLockKey(String operation, Long memberId, AssetCodeEnum assetCode) {
        return String.format(ASSET_LOCK_KEY_PATTERN, operation, memberId, assetCode.getCode());
    }

    private String buildTransferLockKey(Long fromMemberId, String toAccount, AssetCodeEnum assetCode) {
        return String.format(ASSET_TRANSFER_LOCK_KEY_PATTERN, fromMemberId, toAccount, assetCode.getCode());
    }

    // 资产流水构建器

    private static class AssetFlowBuilder {
        private Long memberId;
        private Long assetId;
        private Long fromMemberId;
        private Long toMemberId;
        private BigDecimal num;
        private BigDecimal beforeAvailableNum;
        private BigDecimal afterAvailableNum;
        private String type;
        private String sceneCode;
        private String sceneDescription;
        private String reqNo;
        private String remark;

        private AssetFlowBuilder() {
        }

        public static AssetFlowBuilder create() {
            return new AssetFlowBuilder();
        }

        public AssetFlowBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public AssetFlowBuilder assetId(Long assetId) {
            this.assetId = assetId;
            return this;
        }

        public AssetFlowBuilder fromMemberId(Long fromMemberId) {
            this.fromMemberId = fromMemberId;
            return this;
        }

        public AssetFlowBuilder toMemberId(Long toMemberId) {
            this.toMemberId = toMemberId;
            return this;
        }

        public AssetFlowBuilder num(BigDecimal num) {
            this.num = num;
            return this;
        }

        public AssetFlowBuilder beforeAvailable(BigDecimal beforeAvailableNum) {
            this.beforeAvailableNum = beforeAvailableNum;
            return this;
        }

        public AssetFlowBuilder afterAvailable(BigDecimal afterAvailableNum) {
            this.afterAvailableNum = afterAvailableNum;
            return this;
        }

        public AssetFlowBuilder type(String type) {
            this.type = type;
            return this;
        }

        public AssetFlowBuilder sceneCode(String sceneCode) {
            this.sceneCode = sceneCode;
            return this;
        }

        public AssetFlowBuilder sceneDescription(String sceneDescription) {
            this.sceneDescription = sceneDescription;
            return this;
        }

        public AssetFlowBuilder reqNo(String reqNo) {
            this.reqNo = reqNo;
            return this;
        }

        public AssetFlowBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public UmsMemberAssetsFlow build() {
            UmsMemberAssetsFlow flow = new UmsMemberAssetsFlow();
            flow.setMemberId(this.memberId);
            flow.setAssetId(this.assetId);
            flow.setFromMemberId(this.fromMemberId);
            flow.setToMemberId(this.toMemberId);
            flow.setNum(this.num);
            flow.setBeforeAvailableNum(this.beforeAvailableNum);
            flow.setAfterAvailableNum(this.afterAvailableNum);
            flow.setType(this.type);
            flow.setSceneCode(this.sceneCode);
            flow.setSceneDescription(this.sceneDescription);
            flow.setReqNo(this.reqNo);
            flow.setRemark(this.remark);
            return flow;
        }

        public void buildAndSave(UmsMemberAssetsFlowMapper mapper) {
            mapper.insert(build());
        }
    }
}
