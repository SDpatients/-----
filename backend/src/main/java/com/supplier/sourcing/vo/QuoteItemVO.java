package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteItemVO {

    private Long id;
    private Long quoteId;
    private Long rfqItemId;
    private String materialCode;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal taxPrice;
    private BigDecimal taxRate;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private Integer deliveryDays;
    private String remark;
    private LocalDateTime createTime;
}