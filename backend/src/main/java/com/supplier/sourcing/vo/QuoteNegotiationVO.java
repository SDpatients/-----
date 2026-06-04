package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteNegotiationVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long quoteId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqId;
    @JsonSerialize(using = ToStringSerializer.class)
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