package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.SupplierBlacklistCreateDTO;
import com.supplier.sourcing.dto.SupplierBlacklistUpdateDTO;
import com.supplier.sourcing.query.SupplierBlacklistQuery;
import com.supplier.sourcing.vo.SupplierBlacklistVO;

public interface SupplierBlacklistService {

    PageResult<SupplierBlacklistVO> page(SupplierBlacklistQuery query);

    SupplierBlacklistVO getDetail(Long id);

    Long create(SupplierBlacklistCreateDTO dto);

    void update(Long id, SupplierBlacklistUpdateDTO dto);

    void remove(Long id);
}