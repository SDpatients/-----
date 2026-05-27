package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReceiptAdjustDTO {
    @NotNull(message = "差异数量不能为空")
    private java.math.BigDecimal diffQty;
    @NotNull(message = "差异原因不能为空")
    private String diffReason;
    @NotNull(message = "处理方式不能为空")
    private Integer handleMethod;
    private String handleRemark;
    private String remark;
}