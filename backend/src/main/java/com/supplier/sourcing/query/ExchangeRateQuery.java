package com.supplier.sourcing.query;

import lombok.Data;

@Data
public class ExchangeRateQuery {

    private String fromCurrency;
    private String toCurrency;
    private Long pageNum = 1L;
    private Long pageSize = 10L;
}