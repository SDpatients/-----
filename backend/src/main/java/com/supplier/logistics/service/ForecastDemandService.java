package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.DemandActionDTO;
import com.supplier.logistics.dto.ForecastDemandCreateDTO;
import com.supplier.logistics.query.ForecastDemandQuery;
import com.supplier.logistics.vo.ForecastDemandVO;

public interface ForecastDemandService {

    PageResult<ForecastDemandVO> page(ForecastDemandQuery query);

    ForecastDemandVO getDetail(Long id);

    Long create(ForecastDemandCreateDTO dto);

    void publish(Long id, DemandActionDTO dto);

    void respond(Long id, DemandActionDTO dto);

    void close(Long id, DemandActionDTO dto);
}