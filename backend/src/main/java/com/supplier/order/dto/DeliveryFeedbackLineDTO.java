package com.supplier.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryFeedbackLineDTO {

    @NotNull(message = "订单明细ID不能为空")
    private Long orderDetailId;

    @NotNull(message = "承诺交期不能为空")
    private LocalDate promisedDeliveryDate;

    @NotNull(message = "计划交付数量不能为空")
    @Positive(message = "计划交付数量必须大于0")
    private BigDecimal plannedQuantity;

    private String batchNo;
    private String remark;
}