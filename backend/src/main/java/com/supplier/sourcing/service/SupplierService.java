package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.SupplierCreateDTO;
import com.supplier.sourcing.dto.SupplierRegisterDTO;
import com.supplier.sourcing.query.SupplierQuery;
import com.supplier.sourcing.vo.SupplierVO;

public interface SupplierService {

    PageResult<SupplierVO> page(SupplierQuery query);

    SupplierVO getDetail(Long id);

    /** 管理员创建供应商 */
    Long create(SupplierCreateDTO dto);

    /** 供应商自助注册 */
    Long register(SupplierRegisterDTO dto);

    /** 禁用/启用供应商 */
    void toggleStatus(Long id, Integer status);
}