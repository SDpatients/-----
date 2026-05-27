package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceCreateDTO {

    @NotBlank(message = "发票号码不能为空")
    private String invoiceNo;

    private String invoiceCode;
    private Integer invoiceType = 1;
    private Long reconId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String supplierName;
    private String taxNumber;

    @NotNull(message = "发票金额不能为空")
    private BigDecimal invoiceAmount;

    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private LocalDate invoiceDate;
    private String remark;
}