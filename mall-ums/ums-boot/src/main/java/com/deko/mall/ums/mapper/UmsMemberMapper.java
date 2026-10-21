package com.deko.mall.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deko.mall.ums.model.entity.UmsMember;
import com.deko.mall.ums.model.form.UmsMemberQuery;
import com.deko.mall.ums.model.vo.MemberVO;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface UmsMemberMapper extends BaseMapper<UmsMember> {

    @Select("<script>" +
            " SELECT * from ums_member " +
            " <if test ='nickname !=null and nickname.trim() neq \"\" ' >" +
            "       WHERE nick_name like concat('%',#{nickname},'%')" +
            " </if>" +
            " ORDER BY update_time DESC, create_time DESC" +
            "</script>")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(property = "addressList", column = "id", many = @Many(select = "com.deko.mall.ums.mapper.UmsAddressMapper.listByUserId"))
    })
    List<UmsMember> list(Page<UmsMember> page, String nickname);

    UmsMember selectMemberIdByMobile(@Param("mobile") String mobile);

    UmsMember selectMemberIdByUserId(@Param("userId") Long userId);

    /**
     * 分页查询会员列表
     */
    Page<MemberVO> selectMemberPage(Page<MemberVO> page, @Param("query") UmsMemberQuery query);

    /**
     * 查询当前登录会员信息
     */
    MemberVO getCurrentMemberInfo(@Param("memberId") Long memberId);
}
