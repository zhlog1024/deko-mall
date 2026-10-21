package com.deko.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.entity.PmsSpuGroupRelation;
import com.deko.mall.pms.model.vo.PmsSpuRenderVO;
import com.deko.mall.pms.model.vo.SpuPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分组关系数据访问层
 */
@Mapper
public interface PmsSpuGroupRelationMapper extends BaseMapper<PmsSpuGroupRelation> {

    /**
     * 获取分组中的商品SPU ID列表（按排序）
     *
     * @param groupId 分组ID
     * @return 商品SPU ID列表
     */
    @Select("SELECT spu_id FROM pms_spu_group_relation WHERE group_id = #{groupId} ORDER BY sort ASC")
    List<Long> getSpuIdsByGroupId(@Param("groupId") Long groupId);

    /**
     * 分页获取分组中的商品SPU ID列表
     *
     * @param page    分页参数
     * @param groupId 分组ID
     * @return 包含总数和当前页ID列表的分页对象
     */
    @Select("SELECT spu_id FROM pms_spu_group_relation WHERE group_id = #{groupId} ORDER BY sort ASC")
    IPage<Long> getSpuIdsByGroupIdPaged(Page<Long> page, @Param("groupId") Long groupId);

    /**
     * 分页获取分组商品（按 SpuPageVO 标准字段）
     *
     * @param page    分页参数
     * @param groupId 分组ID
     * @return 包含总数和当前页商品信息的分页对象
     */
    IPage<PmsSpuRenderVO> pageGroupSpuRenderVO(Page<PmsSpuRenderVO> page, @Param("groupId") Long groupId);

    /**
     * 分页获取分组商品（按 SpuPageVO 标准字段，仅返回在售商品）
     * 用于装修组件场景
     *
     * @param page    分页参数
     * @param groupId 分组ID
     * @return 包含总数和当前页商品信息的分页对象
     */
    IPage<SpuPageVO> pageGroupSpuForSpuPageVO(Page<SpuPageVO> page, @Param("groupId") Long groupId);
}

