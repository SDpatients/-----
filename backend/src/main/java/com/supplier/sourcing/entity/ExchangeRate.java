package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exchange_rate")
public class ExchangeRate extends BaseEntity {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private LocalDate effectiveDate;
    private String source;
    private String remark;
}