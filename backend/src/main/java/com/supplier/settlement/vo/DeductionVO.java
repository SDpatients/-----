package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DeductionVO {

    private Long id;
    private String deductionNo;
    private Long supplierId;
    private String sourceType;
    private Long sourceId;
    private Integer deductionType;
    private BigDecimal deductionAmount;
    private String deductionReason;
    private Integer deductionStatus;
    private Long reconId;
    private LocalDateTime createTime;
}