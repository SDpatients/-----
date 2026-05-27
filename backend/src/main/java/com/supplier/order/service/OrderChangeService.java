package com.supplier.order.service;

import com.supplier.common.result.PageResult;
import com.supplier.order.dto.OrderChangeApproveDTO;
import com.supplier.order.dto.OrderChangeCreateDTO;
import com.supplier.order.query.OrderChangeQuery;
import com.supplier.order.vo.OrderChangeVO;

public interface OrderChangeService {
    PageResult<OrderChangeVO> page(OrderChangeQuery query);
    OrderChangeVO getDetail(Long id);
    Long create(OrderChangeCreateDTO dto);
    void approve(Long id, OrderChangeApproveDTO dto);
}
