package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_award")
public class QuoteAward extends BaseEntity {

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
}