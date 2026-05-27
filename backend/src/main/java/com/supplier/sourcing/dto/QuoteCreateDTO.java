package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteCreateDTO {

    @NotBlank(message = "报价单号不能为空")
    private String quoteNo;

    @NotNull(message = "询价单ID不能为空")
    private Long rfqId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String paymentTerms;
    private LocalDateTime validUntil;
    private String remark;
}