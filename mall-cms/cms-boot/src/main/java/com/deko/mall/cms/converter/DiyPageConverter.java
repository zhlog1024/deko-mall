package com.deko.mall.cms.converter;

import com.deko.mall.cms.model.dto.DiyPageSaveDTO;
import com.deko.mall.cms.model.entity.DiyPage;
import com.deko.mall.cms.model.vo.DiyPageVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiyPageConverter {

    DiyPage toEntity(DiyPageSaveDTO dto);

    DiyPageVO toVO(DiyPage entity);
}

