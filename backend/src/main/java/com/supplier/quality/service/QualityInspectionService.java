package com.supplier.quality.service;

import com.supplier.common.result.PageResult;
import com.supplier.quality.dto.QualityHandleDTO;
import com.supplier.quality.dto.QualityInspectionCreateDTO;
import com.supplier.quality.dto.QualitySubmitDTO;
import com.supplier.quality.query.QualityInspectionQuery;
import com.supplier.quality.vo.QualityInspectionVO;

import java.util.List;
import java.util.Map;

public interface QualityInspectionService {
    PageResult<QualityInspectionVO> page(QualityInspectionQuery query);
    QualityInspectionVO getDetail(Long id);
    Long create(QualityInspectionCreateDTO dto);
    void submit(Long id, QualitySubmitDTO dto);
    void handle(Long id, QualityHandleDTO dto);

    /** 查询质检单明细行 */
    List<Map<String, Object>> getLines(Long id);

    /** 从收货记录创建质检任务 */
    Long createFromReceipt(Long receiptId, Map<String, Object> data);
}
