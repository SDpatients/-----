package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReconciliationDetailVO {
    private Long id;
    private Long reconId;
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
    private LocalDateTime confirmTime;
    private String confirmRemark;
    private String remark;
}
