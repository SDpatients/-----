package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteAwardVO {

    private Long id;
    private Long rfqId;
    private Long quoteId;
    private Long supplierId;
    private BigDecimal awardAmount;
    private BigDecimal awardTaxAmount;
    private String awardCurrency;
    private Long orderId;
    private String orderNo;
    private Long awardBy;
    private String awardByName;
    private LocalDateTime awardTime;
    private String remark;
    private LocalDateTime createTime;
}