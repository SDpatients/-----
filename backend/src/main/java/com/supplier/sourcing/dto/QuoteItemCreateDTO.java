package com.supplier.sourcing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QuoteItemCreateDTO {

    private Long rfqLineId;

    private String materialCode;

    private String materialName;

    private String spec;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private BigDecimal taxRate;

    private BigDecimal taxAmount;

    private LocalDate deliveryDate;

    private String paymentTerms;

    private String remark;
}