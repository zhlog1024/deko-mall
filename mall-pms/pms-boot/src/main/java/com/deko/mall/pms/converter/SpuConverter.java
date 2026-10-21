package com.deko.mall.pms.converter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.pms.model.dto.PmsSpuDTO;
import com.deko.mall.pms.model.entity.PmsSpu;
import com.deko.mall.pms.model.form.PmsSpuForm;
import com.deko.mall.pms.model.vo.PmsSpuPageVO;
import com.deko.mall.pms.model.vo.PmsSpuVO;
import com.deko.mall.pms.model.vo.SeckillingSpuVO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 商品对象转换器
 */
@Mapper(componentModel = "spring")
public interface SpuConverter {

    @Mappings({
            @Mapping(target = "album", source = "subPicUrls")
    })
    PmsSpu form2Entity(PmsSpuForm form);

    @InheritInverseConfiguration(name = "form2Entity")
    PmsSpuForm entity2Form(PmsSpu entity);

    /**
     * 实体转分页VO
     *
     * @param entity 商品实体
     * @return 分页VO
     */
    PmsSpuPageVO entity2PageVO(PmsSpu entity);

    /**
     * 实体列表转分页VO列表
     *
     * @param entities 商品实体列表
     * @return 分页VO列表
     */
    List<PmsSpuPageVO> entity2PageVO(List<PmsSpu> entities);

    /**
     * 商品分页对象转换为VO分页对象
     *
     * @param page 商品分页对象
     * @return VO分页对象
     */
    default IPage<PmsSpuPageVO> pageToPageVO(Page<PmsSpu> page) {
        List<PmsSpuPageVO> records = entity2PageVO(page.getRecords());
        Page<PmsSpuPageVO> pageVO = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        pageVO.setRecords(records);
        return pageVO;
    }

    SeckillingSpuVO entity2SeckillingVO(PmsSpu entity);

    List<SeckillingSpuVO> entity2SeckillingVO(List<PmsSpu> entities);

    List<PmsSpuDTO> entity2DTO(List<PmsSpu> pmsSpus);


    List<PmsSpuVO> entity2VOList(List<PmsSpu> list);
}
