package com.deko.mall.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.oms.model.entity.order.OmsOrderAfterSale;
import com.deko.mall.oms.model.query.AfterSalePageQuery;
import com.deko.mall.oms.model.vo.AfterSaleExportVO;
import com.deko.mall.oms.model.vo.AfterSaleStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单售后Mapper接口
 */
@Mapper
public interface OrderAfterSaleMapper extends BaseMapper<OmsOrderAfterSale> {

    /**
     * 分页查询售后单列表（通过 XML baseWhere 复用条件）
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<OmsOrderAfterSale> selectPageByQuery(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<OmsOrderAfterSale> page,
            @Param("queryParams") AfterSalePageQuery queryParams
    );

    /**
     * 查询售后单导出数据列表
     *
     * @param queryParams 查询参数
     * @return 售后单导出数据列表
     */
    List<AfterSaleExportVO> selectAfterSaleExportList(AfterSalePageQuery queryParams);

    /**
     * 一次查询获取所有售后单统计数据
     *
     * @param queryParams       查询参数
     * @param waitAuditStatus   待审核状态值
     * @param waitReceiveStatus 待收货状态值
     * @param refundingStatus   退款中状态值
     * @return 售后单统计信息
     */
    AfterSaleStatisticsVO getAfterSaleStatistics(
            @Param("queryParams") AfterSalePageQuery queryParams,
            @Param("waitAuditStatus") Integer waitAuditStatus,
            @Param("waitReceiveStatus") Integer waitReceiveStatus,
            @Param("refundingStatus") Integer refundingStatus
    );
}
