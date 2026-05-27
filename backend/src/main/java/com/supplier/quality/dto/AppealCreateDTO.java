package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 申诉创建 DTO（联动9: 质检申诉 → 申诉通过后关联扣款调整）
 */
@Data
public class AppealCreateDTO {

    @NotNull(message = "NCR ID不能为空")
    private Long ncrId;

    private Long inspectionId;

    private Long deductionId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String materialCode;

    private String materialName;

    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;

    private String appealDesc;

    /** 申诉要求调整的金额 */
    private BigDecimal adjustAmount;
}