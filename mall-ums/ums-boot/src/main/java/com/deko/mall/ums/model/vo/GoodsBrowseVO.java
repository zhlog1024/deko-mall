package com.deko.mall.ums.model.vo;

import com.deko.mall.pms.model.dto.PmsSpuDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsBrowseVO extends PmsSpuDTO {

    private Long browseId;
}
