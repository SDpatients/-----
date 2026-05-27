package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryDetailItemDTO {
    @NotNull(message = "订单明细ID不能为空")
    private Long orderDetailId;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String unit;
    private BigDecimal actualQty = BigDecimal.ZERO;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String remark;
}