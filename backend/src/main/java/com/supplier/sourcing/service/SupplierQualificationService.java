package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.SupplierQualificationCreateDTO;
import com.supplier.sourcing.dto.SupplierQualificationUpdateDTO;
import com.supplier.sourcing.query.SupplierQualificationQuery;
import com.supplier.sourcing.vo.SupplierQualificationVO;

public interface SupplierQualificationService {

    PageResult<SupplierQualificationVO> page(SupplierQualificationQuery query);

    SupplierQualificationVO getDetail(Long id);

    Long create(SupplierQualificationCreateDTO dto);

    void update(Long id, SupplierQualificationUpdateDTO dto);

    void delete(Long id);
}