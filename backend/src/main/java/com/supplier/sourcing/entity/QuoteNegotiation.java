package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_negotiation")
public class QuoteNegotiation extends BaseEntity {

    private Long quoteId;
    private Long rfqId;
    private Long supplierId;
    private Integer round;
    private String initiator;
    private BigDecimal targetPrice;
    private BigDecimal supplierPrice;
    private String buyerRemark;
    private String supplierRemark;
    private LocalDateTime negotiationTime;
}