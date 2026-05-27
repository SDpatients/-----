package com.supplier.logistics.service;

import com.supplier.logistics.dto.DeliveryDetailCreateDTO;
import com.supplier.logistics.dto.DeliveryDetailUpdateDTO;
import com.supplier.logistics.query.DeliveryDetailQuery;
import com.supplier.logistics.vo.DeliveryDetailVO;

import java.util.List;

public interface DeliveryDetailService {
    List<DeliveryDetailVO> list(DeliveryDetailQuery query);
    DeliveryDetailVO getDetail(Long id);
    Long create(DeliveryDetailCreateDTO dto);
    void update(Long id, DeliveryDetailUpdateDTO dto);
    void delete(Long id);
}
