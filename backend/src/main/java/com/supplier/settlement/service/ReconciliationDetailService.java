package com.supplier.settlement.service;

import com.supplier.settlement.dto.ReconciliationDetailCreateDTO;
import com.supplier.settlement.dto.ReconciliationDetailUpdateDTO;
import com.supplier.settlement.query.ReconciliationDetailQuery;
import com.supplier.settlement.vo.ReconciliationDetailVO;

import java.util.List;

public interface ReconciliationDetailService {
    List<ReconciliationDetailVO> list(ReconciliationDetailQuery query);
    ReconciliationDetailVO getDetail(Long id);
    Long create(ReconciliationDetailCreateDTO dto);
    void update(Long id, ReconciliationDetailUpdateDTO dto);
    void delete(Long id);
}
