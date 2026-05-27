package com.supplier.integration.service;

import com.supplier.common.result.PageResult;
import com.supplier.integration.query.IntegrationLogQuery;
import com.supplier.integration.vo.IntegrationLogVO;

public interface IntegrationLogService {

    PageResult<IntegrationLogVO> page(IntegrationLogQuery query);

    IntegrationLogVO getDetail(Long id);
}