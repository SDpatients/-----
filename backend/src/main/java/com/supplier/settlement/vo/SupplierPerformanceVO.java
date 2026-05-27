package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierPerformanceVO {

    private Long id;
    private Long supplierId;
    private String evaluatePeriod;
    private BigDecimal qualityScore;
    private BigDecimal deliveryScore;
    private BigDecimal serviceScore;
    private BigDecimal priceScore;
    private BigDecimal totalScore;
    private BigDecimal qualifiedRate;
    private BigDecimal ontimeRate;
    private Long evaluateBy;
    private LocalDateTime evaluateTime;
    private String remark;
    private LocalDateTime createTime;
}