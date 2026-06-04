package com.supplier.settlement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String paymentNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long invoiceId;
    private String invoiceNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reconId;
    @JsonSerialize(using = ToStringSerializer.class)
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