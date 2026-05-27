package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NonconformanceReportCreateDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String materialCode;
    private String materialName;
    private BigDecimal unqualifiedQty;

    @NotBlank(message = "问题描述不能为空")
    private String problemDesc;

    private Integer severity;
    private Long inspectionId;
}