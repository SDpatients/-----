package com.supplier.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.system.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}
