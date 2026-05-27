package com.supplier.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.common.entity.SysIdempotentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysIdempotentRecordMapper extends BaseMapper<SysIdempotentRecord> {
}