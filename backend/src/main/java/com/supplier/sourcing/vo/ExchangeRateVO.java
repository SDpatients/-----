package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExchangeRateVO {

    private Long id;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private LocalDate effectiveDate;
    private String source;
    private String remark;
    private LocalDateTime createTime;
}