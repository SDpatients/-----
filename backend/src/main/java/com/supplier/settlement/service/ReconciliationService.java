package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.ReconciliationConfirmDTO;
import com.supplier.settlement.dto.ReconciliationCreateDTO;
import com.supplier.settlement.query.ReconciliationQuery;
import com.supplier.settlement.vo.ReconciliationVO;

public interface ReconciliationService {
    PageResult<ReconciliationVO> page(ReconciliationQuery query);
    ReconciliationVO getDetail(Long id);
    Long create(ReconciliationCreateDTO dto);
    void send(Long id);
    void confirm(Long id, ReconciliationConfirmDTO dto);
}
