package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierPerformanceUpdateDTO {

    @NotBlank(message = "考评周期不能为空")
    private String evaluatePeriod;

    private BigDecimal qualityScore;
    private BigDecimal deliveryScore;
    private BigDecimal serviceScore;
    private BigDecimal priceScore;
    private BigDecimal qualifiedRate;
    private BigDecimal ontimeRate;
    private String remark;
}