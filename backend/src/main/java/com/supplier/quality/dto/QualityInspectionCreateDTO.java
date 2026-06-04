package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QualityInspectionCreateDTO {
    private Long receiptId;
    private Long deliveryId;
    private Long supplierId;
    private Long standardId;
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private BigDecimal inspectQty = BigDecimal.ZERO;
    private Integer inspectType = 1;
    private String inspectRemark;
}
