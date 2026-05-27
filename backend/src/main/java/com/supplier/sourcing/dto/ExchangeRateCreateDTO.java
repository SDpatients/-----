package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExchangeRateCreateDTO {

    @NotBlank(message = "源币种不能为空")
    private String fromCurrency;

    @NotBlank(message = "目标币种不能为空")
    private String toCurrency;

    @NotNull(message = "汇率不能为空")
    private BigDecimal rate;

    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;

    private String source;
    private String remark;
}