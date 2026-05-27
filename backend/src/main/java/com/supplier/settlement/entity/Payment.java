package com.supplier.settlement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment")
public class Payment extends BaseEntity {

    private String paymentNo;
    private Long invoiceId;
    private String invoiceNo;
    private Long reconId;
    private Long supplierId;
    private String supplierName;
    private BigDecimal paymentAmount;
    private Integer paymentMethod;
    private String paymentAccount;
    private String paymentBank;
    private String receiveAccount;
    private String receiveBank;
    private LocalDate scheduleDate;
    private String paymentTerms;
    private LocalDateTime paymentTime;
    private Integer paymentStatus;
    private String receiptNo;
    private Integer approveStatus;
    private String voucherNo;
    private String remark;
}