package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NonconformanceReportCreateDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    @NotBlank(message = "物料名称不能为空")
    private String materialName;

    @NotNull(message = "不合格数量不能为空")
    private BigDecimal unqualifiedQty;

    @NotBlank(message = "问题描述不能为空")
    private String problemDesc;

    @NotNull(message = "严重程度不能为空")
    private Integer severity;
    private Long inspectionId;
}