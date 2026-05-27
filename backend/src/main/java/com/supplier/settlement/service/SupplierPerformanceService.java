package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.SupplierPerformanceCreateDTO;
import com.supplier.settlement.dto.SupplierPerformanceUpdateDTO;
import com.supplier.settlement.query.SupplierPerformanceQuery;
import com.supplier.settlement.vo.SupplierPerformanceVO;

public interface SupplierPerformanceService {

    PageResult<SupplierPerformanceVO> page(SupplierPerformanceQuery query);

    SupplierPerformanceVO getDetail(Long id);

    Long create(SupplierPerformanceCreateDTO dto);

    void update(Long id, SupplierPerformanceUpdateDTO dto);

    void delete(Long id);
}