package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentApprovalActionDTO {

    @NotNull(message = "审批结果不能为空")
    private Integer approvalStatus;

    private String approveRemark;
}