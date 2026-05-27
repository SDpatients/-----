package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.PricingDTO;
import com.supplier.sourcing.dto.RfqCreateDTO;
import com.supplier.sourcing.dto.RfqUpdateDTO;
import com.supplier.sourcing.query.RfqQuery;
import com.supplier.sourcing.vo.RfqVO;

public interface RfqService {

    PageResult<RfqVO> page(RfqQuery query);

    RfqVO getDetail(Long id);

    Long create(RfqCreateDTO dto);

    void update(Long id, RfqUpdateDTO dto);

    void publish(Long id);

    void close(Long id);

    void cancel(Long id);

    void price(Long id, PricingDTO dto);
}