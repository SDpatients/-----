package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VmiInventorySyncDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    private Long warehouseId;
    private String warehouseName;

    @NotNull(message = "现有库存不能为空")
    private BigDecimal onhandQty;

    @NotNull(message = "可用库存不能为空")
    private BigDecimal availableQty;

    private BigDecimal safetyQty;
    private BigDecimal maxQty;
}