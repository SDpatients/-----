package com.supplier.settlement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentApprovalVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long paymentId;
    private String paymentNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String supplierName;
    private BigDecimal paymentAmount;
    private Integer approvalLevel;
    private Integer approvalStatus;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long approverId;
    private String approverName;
    private String approveRemark;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
}