package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeductionCreateDTO {

    @NotBlank(message = "扣款单号不能为空")
    private String deductionNo;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "来源类型不能为空")
    private String sourceType;

    private Long sourceId;

    @NotNull(message = "扣款类型不能为空")
    private Integer deductionType;

    @NotNull(message = "扣款金额不能为空")
    private BigDecimal deductionAmount;

    @NotBlank(message = "扣款原因不能为空")
    private String deductionReason;

    private Long reconId;
}