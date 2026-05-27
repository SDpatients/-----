package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.ExchangeRateCreateDTO;
import com.supplier.sourcing.dto.ExchangeRateUpdateDTO;
import com.supplier.sourcing.query.ExchangeRateQuery;
import com.supplier.sourcing.vo.ExchangeRateVO;

import java.math.BigDecimal;

public interface ExchangeRateService {

    PageResult<ExchangeRateVO> page(ExchangeRateQuery query);

    ExchangeRateVO getDetail(Long id);

    Long create(ExchangeRateCreateDTO dto);

    void update(Long id, ExchangeRateUpdateDTO dto);

    void delete(Long id);

    BigDecimal getRate(String fromCurrency, String toCurrency);
}