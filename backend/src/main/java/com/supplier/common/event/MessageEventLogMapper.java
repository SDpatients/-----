package com.supplier.common.event;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageEventLogMapper extends BaseMapper<MessageEventLog> {
}