package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteVO {

    private Long id;
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
    private LocalDateTime createTime;
}