package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.DeliveryTemplateCreateDTO;
import com.supplier.logistics.query.DeliveryTemplateQuery;
import com.supplier.logistics.vo.DeliveryTemplateVO;

public interface DeliveryTemplateService {
    PageResult<DeliveryTemplateVO> page(DeliveryTemplateQuery query);
    DeliveryTemplateVO getDetail(Long id);
    Long create(DeliveryTemplateCreateDTO dto);
    void update(Long id, DeliveryTemplateCreateDTO dto);
    void delete(Long id);
}