package com.supplier.settlement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deduction")
public class Deduction extends BaseEntity {

    private String deductionNo;
    private Long supplierId;
    private String sourceType;
    private Long sourceId;
    private Integer deductionType;
    private BigDecimal deductionAmount;
    private String deductionReason;
    private Integer deductionStatus;
    private Long reconId;
}