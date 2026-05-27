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
@TableName("invoice")
public class Invoice extends BaseEntity {

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
}