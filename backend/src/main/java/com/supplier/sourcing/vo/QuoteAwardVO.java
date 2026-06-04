package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteAwardVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long quoteId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private BigDecimal awardAmount;
    private BigDecimal awardTaxAmount;
    private String awardCurrency;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long awardBy;
    private String awardByName;
    private LocalDateTime awardTime;
    private String remark;
    private LocalDateTime createTime;
}