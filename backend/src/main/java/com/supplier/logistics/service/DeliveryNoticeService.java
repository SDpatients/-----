package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.DeliveryActionDTO;
import com.supplier.logistics.dto.DeliveryNoticeCreateDTO;
import com.supplier.logistics.query.DeliveryNoticeQuery;
import com.supplier.logistics.vo.DeliveryNoticeVO;

public interface DeliveryNoticeService {
    PageResult<DeliveryNoticeVO> page(DeliveryNoticeQuery query);
    DeliveryNoticeVO getDetail(Long id);
    Long create(DeliveryNoticeCreateDTO dto);
    void ship(Long id, DeliveryActionDTO dto);
    void arrive(Long id, DeliveryActionDTO dto);
}
