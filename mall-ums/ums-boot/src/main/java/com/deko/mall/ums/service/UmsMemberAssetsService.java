package com.deko.mall.ums.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.ums.dto.AssetOperateRequest;
import com.deko.mall.ums.dto.MemberAssetsDTO;
import com.deko.mall.ums.enums.AssetCodeEnum;
import com.deko.mall.ums.model.domain.UmsMemberAssets;
import com.deko.mall.ums.model.domain.UmsMemberAssetsFlow;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员资产服务接口
 * <p>
 * 设计原则：
 * - 查询类：独立方法，保持语义清晰
 * - 操作类：统一入口，通过 AssetOperateRequest 封装参数
 */
public interface UmsMemberAssetsService extends IService<UmsMemberAssets> {

    // 查询方法

    /**
     * 根据会员ID和资产类型获取会员资产
     *
     * @param memberId  会员ID
     * @param assetCode 资产类型
     * @return 会员资产
     */
    MemberAssetsDTO getMemberAsset(Long memberId, String assetCode);

    /**
     * 根据会员ID获取会员资产列表
     *
     * @param memberId 会员ID
     * @return 会员资产列表
     */
    List<MemberAssetsDTO> getMemberAssets(Long memberId);

    /**
     * 资产流水
     *
     * @param memberId 会员ID
     * @param id       流水ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 积分流水列表
     */
    Page<UmsMemberAssetsFlow> flowAsset(Long memberId, Long id, Page<UmsMemberAssetsFlow> objectPage);

    /**
     * 根据会员ID和资产类型获取资产流水列表
     *
     * @param memberId  会员ID
     * @param assetCode 资产类型
     * @param sceneCode 业务场景编码
     * @param reqNo     业务单号
     * @return 资产流水列表
     */
    Page<UmsMemberAssetsFlow> flowAssetByAssetCode(Long memberId, String assetCode, String sceneCode, String reqNo, Page<UmsMemberAssetsFlow> objectPage);

    /**
     * 获取订单锁定的资产数量
     *
     * @param memberId 会员ID
     * @param orderNo  订单号
     * @return 锁定数量
     */
    UmsMemberAssetsFlow getOrderLockNum(Long memberId, String orderNo);

    List<Long> getNeedInitUser();

    // 操作方法（统一入口）

    /**
     * 通用资产操作
     * 统一处理：订单、转账、内部调整等场景
     *
     * @param request 资产操作请求
     * @return 操作结果
     */
    boolean operate(AssetOperateRequest request);

    /**
     * 积分转账
     * 便捷方法，内部调用 operate(TRANSFER)
     *
     * @param memberId       转出会员ID
     * @param receiverMobile 接收方手机号
     * @param num            转账数量
     * @param remark         备注
     * @return 操作结果
     */
    boolean transferAssets(Long memberId, String receiverMobile, BigDecimal num, String remark);

    // 初始化方法

    /**
     * 初始化会员资产
     *
     * @param memberId  会员ID
     * @param assetCode 资产类型
     * @return 会员资产
     */
    UmsMemberAssets initMemberAssets(Long memberId, AssetCodeEnum assetCode);

    /**
     * 批量初始化会员资产
     *
     * @param memberId 会员ID
     */
    void initAllMemberAssets(Long memberId);

    // 查询类辅助方法（内部使用）

    /**
     * 根据会员ID获取会员资产列表
     *
     * @param userIds 会员ID
     * @return 会员资产列表
     */
    List<UmsMemberAssets> getMemberInfoByUserId(List<Long> userIds);
}
