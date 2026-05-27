package com.supplier.order.service;

import com.supplier.order.dto.PurchaseOrderDetailCreateDTO;
import com.supplier.order.dto.PurchaseOrderDetailUpdateDTO;
import com.supplier.order.query.PurchaseOrderDetailQuery;
import com.supplier.order.vo.PurchaseOrderDetailVO;

import java.util.List;

public interface PurchaseOrderDetailService {
    List<PurchaseOrderDetailVO> list(PurchaseOrderDetailQuery query);
    PurchaseOrderDetailVO getDetail(Long id);
    Long create(PurchaseOrderDetailCreateDTO dto);
    void update(Long id, PurchaseOrderDetailUpdateDTO dto);
    void delete(Long id);
}
