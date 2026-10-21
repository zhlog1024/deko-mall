package com.deko.mall.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.ums.model.entity.UmsAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UmsAddressMapper extends BaseMapper<UmsAddress> {

    @Select("<script>" +
            " SELECT * from ums_address where member_id =#{userId} " +
            "</script>")
    List<UmsAddress> listByUserId(Long userId);

}
