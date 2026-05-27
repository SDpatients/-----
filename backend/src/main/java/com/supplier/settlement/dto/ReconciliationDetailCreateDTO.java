package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReconciliationDetailCreateDTO {
    @NotNull(message = "对账单ID不能为空")
    private Long reconId;
    private Long orderId;
    private String orderNo;
    private Long deliveryId;
    private String deliveryNo;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal orderAmount = BigDecimal.ZERO;
    private BigDecimal confirmedAmount = BigDecimal.ZERO;
    private BigDecimal diffAmount = BigDecimal.ZERO;
    private String diffReason;
    private Integer confirmStatus = 0;
    private String confirmRemark;
    private String remark;
}
