package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class QuoteItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long quoteId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqItemId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqLineId; // 兼容前端字段
    private String materialCode;
    private String materialName;
    private String spec;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal unitPrice; // 兼容前端字段
    private BigDecimal taxPrice;
    private BigDecimal taxRate;
    private BigDecimal amount;
    private BigDecimal totalPrice; // 兼容前端字段
    private BigDecimal taxAmount;
    private LocalDate deliveryDate;
    private String paymentTerms;
    private Integer deliveryDays;
    private String remark;
    private LocalDateTime createTime;
}