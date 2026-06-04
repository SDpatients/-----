package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_item")
public class QuoteItem extends BaseEntity {

    private Long quoteId;
    private Long rfqItemId;
    private String materialCode;
    
    private String materialName;
    private String spec;
    private String unit;
    
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal taxPrice;
    private BigDecimal taxRate;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    
    private LocalDate deliveryDate;
    private String paymentTerms;
    
    private Integer deliveryDays;
    private String remark;
}