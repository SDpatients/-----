package com.supplier.settlement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierPerformanceVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String evaluatePeriod;
    private BigDecimal qualityScore;
    private BigDecimal deliveryScore;
    private BigDecimal serviceScore;
    private BigDecimal priceScore;
    private BigDecimal totalScore;
    private BigDecimal qualifiedRate;
    private BigDecimal ontimeRate;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long evaluateBy;
    private LocalDateTime evaluateTime;
    private String remark;
    private LocalDateTime createTime;
}