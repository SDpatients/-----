package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierPerformanceCreateDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "考评周期不能为空")
    private String evaluatePeriod;

    @NotNull(message = "质量评分不能为空")
    private BigDecimal qualityScore;

    @NotNull(message = "交付评分不能为空")
    private BigDecimal deliveryScore;

    @NotNull(message = "服务评分不能为空")
    private BigDecimal serviceScore;

    @NotNull(message = "价格评分不能为空")
    private BigDecimal priceScore;
    private BigDecimal qualifiedRate;
    private BigDecimal ontimeRate;
    private String remark;
}