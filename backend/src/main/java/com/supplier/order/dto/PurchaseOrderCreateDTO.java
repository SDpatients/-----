package com.supplier.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseOrderCreateDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String supplierName;

    @NotNull(message = "订单日期不能为空")
    private LocalDate orderDate;

    private LocalDate deliveryDate;
    private String currency = "CNY";
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal payAmount = BigDecimal.ZERO;
    private String deliveryAddress;
    private String paymentTerms;
    private String remark;
}
