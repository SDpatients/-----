package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReconciliationVO {
    private Long id;
    private String reconNo;
    private Long supplierId;
    private String supplierName;
    private String reconPeriod;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private BigDecimal confirmedAmount;
    private BigDecimal diffAmount;
    private Integer reconStatus;
    private LocalDateTime sendTime;
    private LocalDateTime confirmTime;
    private String confirmRemark;
    private String remark;
}
