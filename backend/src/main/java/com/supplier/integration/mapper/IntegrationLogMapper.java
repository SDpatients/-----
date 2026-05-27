package com.supplier.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.integration.entity.IntegrationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IntegrationLogMapper extends BaseMapper<IntegrationLog> {
}