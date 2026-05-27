package com.supplier.settlement.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReconciliationDetailUpdateDTO {
    private Long orderId;
    private String orderNo;
    private Long deliveryId;
    private String deliveryNo;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal orderAmount;
    private BigDecimal confirmedAmount;
    private BigDecimal diffAmount;
    private String diffReason;
    private Integer confirmStatus;
    private String confirmRemark;
    private String remark;
}
