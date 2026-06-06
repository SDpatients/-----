package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinancialReconciliationRecordVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String asnNo;
    private String orderNo;
    private String supplierName;
    private BigDecimal totalAmount;
    private Integer quantity;
    private String currency;
    private Integer reconciliationStatus;
    private String reconciliationStatusLabel;
    private Integer paymentStatus;
    private String paymentStatusLabel;
    private BigDecimal diffAmount;
    private String period;
    private LocalDate createDate;
}
