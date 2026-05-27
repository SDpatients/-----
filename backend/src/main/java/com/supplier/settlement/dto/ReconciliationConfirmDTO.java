package com.supplier.settlement.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReconciliationConfirmDTO {
    private BigDecimal confirmedAmount = BigDecimal.ZERO;
    private BigDecimal diffAmount = BigDecimal.ZERO;
    private Boolean disputed = false;
    private String confirmRemark;
}
