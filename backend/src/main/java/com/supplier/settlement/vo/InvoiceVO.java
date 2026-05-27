package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InvoiceVO {

    private Long id;
    private String invoiceNo;
    private String invoiceCode;
    private Integer invoiceType;
    private Long reconId;
    private Long supplierId;
    private String supplierName;
    private String taxNumber;
    private BigDecimal invoiceAmount;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private LocalDate invoiceDate;
    private Integer invoiceStatus;
    private LocalDateTime receiveTime;
    private LocalDateTime certifyTime;
    private LocalDateTime voidTime;
    private String voidReason;
    private Long fileId;
    private Integer ocrStatus;
    private String remark;
    private LocalDateTime createTime;
}