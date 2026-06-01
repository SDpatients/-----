package com.supplier.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseOrderDetailCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    private Integer lineNo;
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String materialSpec;
    private String materialModel;
    private String unit;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal taxRate = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal amount = BigDecimal.ZERO;

    @NotNull(message = "交货日期不能为空")
    private LocalDate deliveryDate;
    private String remark;
}
