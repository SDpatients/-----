package com.supplier.sourcing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class QuoteUpdateDTO {

    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String remark;

    private List<QuoteItemUpdateDTO> lines;

    @Data
public static class QuoteItemUpdateDTO {
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
}