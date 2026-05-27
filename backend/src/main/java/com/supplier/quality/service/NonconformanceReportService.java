package com.supplier.quality.service;

import com.supplier.common.result.PageResult;
import com.supplier.quality.dto.NcrActionDTO;
import com.supplier.quality.dto.NonconformanceReportCreateDTO;
import com.supplier.quality.query.NonconformanceReportQuery;
import com.supplier.quality.vo.NonconformanceReportVO;

public interface NonconformanceReportService {

    PageResult<NonconformanceReportVO> page(NonconformanceReportQuery query);

    NonconformanceReportVO getDetail(Long id);

    Long create(NonconformanceReportCreateDTO dto);

    void submit(Long id, NcrActionDTO dto);

    void handle(Long id, NcrActionDTO dto);

    void verify(Long id, NcrActionDTO dto);

    void close(Long id, NcrActionDTO dto);
}