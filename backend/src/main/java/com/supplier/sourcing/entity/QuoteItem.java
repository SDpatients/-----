package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_item")
public class QuoteItem extends BaseEntity {

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
}