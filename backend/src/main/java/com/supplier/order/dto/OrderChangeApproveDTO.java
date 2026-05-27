package com.supplier.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderChangeApproveDTO {
    @NotNull(message = "审批状态不能为空")
    private Integer approveStatus;
    private String approveRemark;
}
