package com.supplier.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.integration.entity.IntegrationEndpoint;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IntegrationEndpointMapper extends BaseMapper<IntegrationEndpoint> {
}