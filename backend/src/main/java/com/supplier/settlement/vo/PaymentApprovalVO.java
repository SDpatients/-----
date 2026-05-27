package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentApprovalVO {

    private Long id;
    private Long paymentId;
    private String paymentNo;
    private Long supplierId;
    private String supplierName;
    private BigDecimal paymentAmount;
    private Integer approvalLevel;
    private Integer approvalStatus;
    private Long approverId;
    private String approverName;
    private String approveRemark;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
}