package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.RfqSupplierCreateDTO;
import com.supplier.sourcing.query.RfqSupplierQuery;
import com.supplier.sourcing.vo.RfqSupplierVO;

public interface RfqSupplierService {

    PageResult<RfqSupplierVO> page(RfqSupplierQuery query);

    RfqSupplierVO getDetail(Long id);

    Long create(RfqSupplierCreateDTO dto);

    void delete(Long id);
}