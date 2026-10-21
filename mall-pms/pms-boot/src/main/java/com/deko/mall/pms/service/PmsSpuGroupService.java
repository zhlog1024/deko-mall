package com.deko.mall.pms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.deko.mall.pms.model.entity.PmsSpuGroup;
import com.deko.mall.pms.model.form.SpuSortItem;
import com.deko.mall.pms.model.query.PmsSpuGroupQuery;
import com.deko.mall.pms.model.vo.PmsSpuGroupRenderVO;
import com.deko.mall.pms.model.vo.PmsSpuRenderVO;
import com.deko.mall.pms.model.vo.PmsSpuVO;
import com.deko.mall.pms.model.vo.SpuPageVO;

import java.util.List;

/**
 * 商品分组服务接口
 */
public interface PmsSpuGroupService extends IService<PmsSpuGroup> {

    /**
     * 分页查询商品分组
     *
     * @param queryParams 查询参数
     * @return 分页结果
     */
    IPage<PmsSpuGroupRenderVO> listSpuGroupPages(PmsSpuGroupQuery queryParams);

    /**
     * 获取所有启用的商品分组
     *
     * @return 商品分组列表
     */
    List<PmsSpuGroupRenderVO> listEnabledSpuGroups();

    /**
     * 获取商品分组详情
     *
     * @param id 分组ID
     * @return 商品分组详情
     */
    PmsSpuGroupRenderVO getSpuGroupDetail(Long id);

    /**
     * 创建商品分组
     *
     * @param goodsGroup 商品分组信息
     * @return 是否成功
     */
    boolean createSpuGroup(PmsSpuGroup goodsGroup);

    /**
     * 更新商品分组
     *
     * @param goodsGroup 商品分组信息
     * @return 是否成功
     */
    boolean updateSpuGroup(PmsSpuGroup goodsGroup);

    /**
     * 删除商品分组
     *
     * @param id 分组ID
     * @return 是否成功
     */
    boolean deleteSpuGroup(Long id);

    /**
     * 获取分组中的商品ID列表
     *
     * @param groupId 分组ID
     * @return 商品SPU ID列表
     */
    List<Long> getGroupSpuIds(Long groupId);

    /**
     * 添加商品到分组
     *
     * @param groupId 分组ID
     * @param spuIds  商品SPU ID列表
     * @return 是否成功
     */
    boolean addSpusToGroup(Long groupId, List<Long> spuIds);

    /**
     * 从分组中移除商品
     *
     * @param groupId 分组ID
     * @param spuIds  商品SPU ID列表
     * @return 是否成功
     */
    boolean removeSpusFromGroup(Long groupId, List<Long> spuIds);

    /**
     * 获取分组商品分页数据
     *
     * @param query 分页查询参数
     * @return 商品分页数据
     */
    IPage<PmsSpuRenderVO> pageGroupSpu(PmsSpuGroupQuery query);

    /**
     * 获取分组商品分页数据（按 SpuPageVO 标准字段，仅返回在售商品）
     * 用于装修组件场景
     *
     * @param query 分页查询参数
     * @return 商品分页数据
     */
    IPage<SpuPageVO> pageGroupSpuForSpuPageVO(PmsSpuGroupQuery query);

    /**
     * 获取分组中的商品详细信息
     *
     * @param groupId 分组ID
     * @return 商品详细信息列表
     */
    List<PmsSpuVO> getGroupSpuDetails(Long groupId);

    /**
     * 创建商品分组（包含商品）
     *
     * @param spuGroup 商品分组信息
     * @param spuList  商品列表（含排序）
     * @return 是否成功
     */
    boolean createSpuGroupWithSpus(PmsSpuGroup spuGroup, List<SpuSortItem> spuList);

    /**
     * 更新商品分组（包含商品）
     *
     * @param spuGroup 商品分组信息
     * @param spuList  商品列表（含排序）
     * @return 是否成功
     */
    boolean updateSpuGroupWithSpus(PmsSpuGroup spuGroup, List<SpuSortItem> spuList);
}

