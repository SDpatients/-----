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
@TableName("reconciliation")
public class Reconciliation extends BaseEntity {
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
    private Long confirmBy;
    private String confirmRemark;
    private LocalDateTime closeTime;
    private String remark;
}
