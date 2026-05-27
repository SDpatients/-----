package com.supplier.settlement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_approval")
public class PaymentApproval extends BaseEntity {

    private Long paymentId;
    private String paymentNo;
    private Long supplierId;
    private String supplierName;
    private java.math.BigDecimal paymentAmount;
    private Integer approvalLevel;
    private Integer approvalStatus;
    private Long approverId;
    private String approverName;
    private String approveRemark;
    private java.time.LocalDateTime approveTime;
}