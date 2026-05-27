package com.supplier.sourcing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExchangeRateUpdateDTO {

    private BigDecimal rate;
    private LocalDate effectiveDate;
    private String source;
    private String remark;
}