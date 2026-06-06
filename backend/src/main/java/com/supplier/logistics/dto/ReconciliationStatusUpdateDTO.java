package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReconciliationStatusUpdateDTO {
    @NotNull(message = "对账状态不能为空")
    private Integer reconciliationStatus;
    /** 差异金额（当对账状态为"有差异"时填写） */
    private BigDecimal diffAmount;
    /** 差异原因 */
    private String diffReason;
}
