package com.deko.mall.pms.converter;

import com.deko.mall.pms.model.entity.PmsSpuGroup;
import com.deko.mall.pms.model.vo.PmsSpuGroupRenderVO;
import org.springframework.stereotype.Component;

/**
 * 商品分组转换器
 */
@Component
public class PmsSpuGroupConverter {

    /**
     * 实体转VO
     *
     * @param entity 实体
     * @return VO
     */
    public PmsSpuGroupRenderVO entity2VO(PmsSpuGroup entity) {
        if (entity == null) {
            return null;
        }
        PmsSpuGroupRenderVO vo = new PmsSpuGroupRenderVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        vo.setIconUrl(entity.getIconUrl());
        return vo;
    }
}

