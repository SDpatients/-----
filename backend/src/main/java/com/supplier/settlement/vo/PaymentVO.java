package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentVO {

    private Long id;
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
    private LocalDateTime createTime;
}