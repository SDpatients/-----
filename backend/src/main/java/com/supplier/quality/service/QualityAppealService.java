package com.supplier.quality.service;

import com.supplier.common.result.PageResult;
import com.supplier.quality.dto.QualityAppealAuditDTO;
import com.supplier.quality.dto.QualityAppealCreateDTO;
import com.supplier.quality.query.QualityAppealQuery;
import com.supplier.quality.vo.QualityAppealVO;

public interface QualityAppealService {

    PageResult<QualityAppealVO> page(QualityAppealQuery query);

    QualityAppealVO getDetail(Long id);

    Long create(QualityAppealCreateDTO dto);

    void submit(Long id);

    void approve(Long id, QualityAppealAuditDTO dto);

    void reject(Long id, QualityAppealAuditDTO dto);
}