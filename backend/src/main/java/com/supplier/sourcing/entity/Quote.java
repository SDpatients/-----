package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote")
public class Quote extends BaseEntity {

    private String quoteNo;
    private Long rfqId;
    private Long supplierId;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private Integer quoteStatus;
    private Integer negotiationRound;
    private String paymentTerms;
    private LocalDateTime submitTime;
    private LocalDateTime validUntil;
    private String remark;
}