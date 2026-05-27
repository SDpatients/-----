package com.supplier.integration.service;

import com.supplier.common.result.PageResult;
import com.supplier.integration.dto.IntegrationEndpointCreateDTO;
import com.supplier.integration.dto.IntegrationEndpointUpdateDTO;
import com.supplier.integration.query.IntegrationEndpointQuery;
import com.supplier.integration.vo.IntegrationEndpointVO;

public interface IntegrationEndpointService {

    PageResult<IntegrationEndpointVO> page(IntegrationEndpointQuery query);

    IntegrationEndpointVO getDetail(Long id);

    Long create(IntegrationEndpointCreateDTO dto);

    void update(Long id, IntegrationEndpointUpdateDTO dto);

    void delete(Long id);

    void enable(Long id);

    void disable(Long id);
}