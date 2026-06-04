package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String quoteNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqId;
    private String rfqNo;
    private String rfqTitle;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String supplierName;
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