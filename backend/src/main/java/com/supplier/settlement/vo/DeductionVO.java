package com.supplier.settlement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DeductionVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String deductionNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String sourceType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;
    private Integer deductionType;
    private BigDecimal deductionAmount;
    private String deductionReason;
    private Integer deductionStatus;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reconId;
    private LocalDateTime createTime;
}