package com.supplier.integration.service;

import com.supplier.common.result.PageResult;
import com.supplier.integration.dto.SyncTaskRetryDTO;
import com.supplier.integration.query.IntegrationSyncTaskQuery;
import com.supplier.integration.vo.IntegrationSyncTaskVO;

public interface IntegrationSyncTaskService {

    PageResult<IntegrationSyncTaskVO> page(IntegrationSyncTaskQuery query);

    IntegrationSyncTaskVO getDetail(Long id);

    void retry(Long id, SyncTaskRetryDTO dto);
}