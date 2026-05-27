package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.SupplierCategoryCreateDTO;
import com.supplier.sourcing.dto.SupplierCategoryUpdateDTO;
import com.supplier.sourcing.query.SupplierCategoryQuery;
import com.supplier.sourcing.vo.SupplierCategoryVO;

import java.util.List;

public interface SupplierCategoryService {

    PageResult<SupplierCategoryVO> page(SupplierCategoryQuery query);

    List<SupplierCategoryVO> listAll();

    SupplierCategoryVO getDetail(Long id);

    Long create(SupplierCategoryCreateDTO dto);

    void update(Long id, SupplierCategoryUpdateDTO dto);

    void delete(Long id);
}