package com.supplier.quality.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QualitySubmitDTO {
    private BigDecimal inspectQty = BigDecimal.ZERO;
    private BigDecimal qualifiedQty = BigDecimal.ZERO;
    private BigDecimal unqualifiedQty = BigDecimal.ZERO;
    private Integer inspectResult;
    private Integer inspectType;
    private String inspectRemark;
}
