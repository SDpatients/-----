package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.DeductionActionDTO;
import com.supplier.settlement.dto.DeductionCreateDTO;
import com.supplier.settlement.query.DeductionQuery;
import com.supplier.settlement.vo.DeductionVO;

public interface DeductionService {

    PageResult<DeductionVO> page(DeductionQuery query);

    DeductionVO getDetail(Long id);

    Long create(DeductionCreateDTO dto);

    void submit(Long id, DeductionActionDTO dto);

    void confirm(Long id, DeductionActionDTO dto);

    void dispute(Long id, DeductionActionDTO dto);

    void book(Long id, DeductionActionDTO dto);
}