package com.supplier.settlement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_performance")
public class SupplierPerformance extends BaseEntity {
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
}