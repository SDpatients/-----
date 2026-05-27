package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteNegotiationVO {

    private Long id;
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
    private LocalDateTime createTime;
}