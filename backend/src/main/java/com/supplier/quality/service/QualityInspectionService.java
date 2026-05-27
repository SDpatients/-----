package com.supplier.quality.service;

import com.supplier.common.result.PageResult;
import com.supplier.quality.dto.QualityHandleDTO;
import com.supplier.quality.dto.QualityInspectionCreateDTO;
import com.supplier.quality.dto.QualitySubmitDTO;
import com.supplier.quality.query.QualityInspectionQuery;
import com.supplier.quality.vo.QualityInspectionVO;

public interface QualityInspectionService {
    PageResult<QualityInspectionVO> page(QualityInspectionQuery query);
    QualityInspectionVO getDetail(Long id);
    Long create(QualityInspectionCreateDTO dto);
    void submit(Long id, QualitySubmitDTO dto);
    void handle(Long id, QualityHandleDTO dto);
}
