package com.deko.mall.ums.model.vo;

import com.deko.mall.ums.dto.MemberAssetsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会员视图对象。
 */
@Schema(description = "会员视图层对象")
@Data
public class MemberVO {

    @Schema(description = "会员ID")
    private Long id;
    @Schema(description = "会员昵称")
    private String nickName;
    @Schema(description = "会员头像地址")
    private String avatarUrl;
    @Schema(description = "会员手机号")
    private String mobile;
    @Schema(description = "角色编码（兼容字段，默认 normal）")
    private String roleCode;
    @Schema(description = "用户资产")
    private List<MemberAssetsVO> assets;
    @Schema(description = "标签名称")
    private String tagName;
    @Schema(description = "会员展示类型")
    private String memberType;
    @Schema(description = "会员状态：1-正常，0-禁用")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "性别 0未知 1男 2女")
    private Integer gender;
    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "未读消息数")
    private Long messageTotal;
    @Schema(description = "订单状态计数 key=status_0~status_6")
    private Map<String, Integer> orderStatusCounts;
}
