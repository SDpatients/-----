package com.supplier.quality.service;

import com.supplier.common.result.PageResult;
import com.supplier.quality.dto.EightDActionDTO;
import com.supplier.quality.dto.EightDReportCreateDTO;
import com.supplier.quality.dto.EightDReportUpdateDTO;
import com.supplier.quality.query.EightDReportQuery;
import com.supplier.quality.vo.EightDReportVO;

public interface EightDReportService {

    PageResult<EightDReportVO> page(EightDReportQuery query);

    EightDReportVO getDetail(Long id);

    Long create(EightDReportCreateDTO dto);

    void update(Long id, EightDReportUpdateDTO dto);

    void submit(Long id, EightDActionDTO dto);

    void audit(Long id, EightDActionDTO dto);

    void reject(Long id, EightDActionDTO dto);

    void close(Long id, EightDActionDTO dto);

    void stepSubmit(Long id, EightDActionDTO dto);

    void stepApprove(Long id, EightDActionDTO dto);
}