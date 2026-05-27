package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QualityAppealCreateDTO {

    private Long ncrId;
    private Long inspectionId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;
}