package com.deko.mall.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deko.mall.system.model.entity.SysMsgRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息记录Mapper接口
 */
@Mapper
public interface MsgRecordMapper extends BaseMapper<SysMsgRecord> {
}
