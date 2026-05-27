package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentApprovalSubmitDTO {

    @NotNull(message = "付款单ID不能为空")
    private Long paymentId;
}