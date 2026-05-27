package com.supplier.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.integration.entity.IntegrationSyncTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IntegrationSyncTaskMapper extends BaseMapper<IntegrationSyncTask> {
}