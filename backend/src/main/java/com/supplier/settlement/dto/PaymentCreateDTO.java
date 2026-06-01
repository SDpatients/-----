package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentCreateDTO {

    @NotBlank(message = "付款单号不能为空")
    private String paymentNo;

    private Long invoiceId;
    private String invoiceNo;
    private Long reconId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String supplierName;

    @NotNull(message = "付款金额不能为空")
    private BigDecimal paymentAmount;

    private Integer paymentMethod = 1;
    private String paymentAccount;
    private String paymentBank;
    private String receiveAccount;
    private String receiveBank;

    @NotNull(message = "计划付款日期不能为空")
    private LocalDate scheduleDate;
    private String paymentTerms;
    private String remark;
}