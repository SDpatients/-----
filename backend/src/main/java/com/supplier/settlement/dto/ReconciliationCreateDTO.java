package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReconciliationCreateDTO {
    @NotBlank(message = "对账单号不能为空")
    private String reconNo;
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;
    private String supplierName;
    @NotBlank(message = "对账周期不能为空")
    private String reconPeriod;

    @NotNull(message = "对账开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "对账结束日期不能为空")
    private LocalDate endDate;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String remark;
}
