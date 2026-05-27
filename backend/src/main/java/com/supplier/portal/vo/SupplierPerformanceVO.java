package com.supplier.portal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPerformanceVO {
    private Long supplierId;
    private String supplierName;
    private BigDecimal deliveryRate;
    private BigDecimal qualityRate;
    private BigDecimal responseRate;
    private Integer score;
}
