package com.deko.mall.pms.model.vo;

import com.deko.mall.pms.model.entity.PmsSpuAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 【管理端】商品详情视图对象
 */
@Data
@Schema(description = "商品详情视图对象")
public class PmsSpuDetailVO {

    private Long id;
    private String name;
    private String spuType;
    private String spuTypeDesc;
    private Long categoryId;
    private Long brandId;
    private BigDecimal originPrice;

    private BigDecimal price;

    @Schema(description = "商品主图")
    private String picUrl;
    @Schema(description = "商品副图")
    private String[] subPicUrls;


    /**
     * 是否需要发货
     */
    @Schema(description = "是否需要发货")
    private Boolean needDelivery;
    private String description;
    private String detail;
    private Long forbiddenAreaId;
    private Long freightTemplateId;
    private List<PmsSpuAttribute> attrList;
    private List<PmsSpuAttribute> specList;
    private List<PmsSkuVO> skuList;
    @Schema(description = "审核状态（0-待审核，1-已通过，2-已拒绝）")
    private Integer auditStatus;
    @Schema(description = "系统备注")
    private String remark;
    @Schema(description = "提交审核时间")
    private LocalDateTime submitTime;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核人")
    private String auditUser;
    @Schema(description = "供应商 ID")
    private Long supplierId;
}
