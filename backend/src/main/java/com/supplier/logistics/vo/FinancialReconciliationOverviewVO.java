package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinancialReconciliationOverviewVO {
    private BigDecimal totalAmount;
    private BigDecimal reconciledAmount;
    private BigDecimal pendingAmount;
    private BigDecimal diffAmount;
    private Integer totalCount;
    private Integer reconciledCount;
    private Integer pendingCount;
    private Integer diffCount;
}
