package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentStatusUpdateDTO {
    @NotNull(message = "付款状态不能为空")
    private Integer paymentStatus;
}
