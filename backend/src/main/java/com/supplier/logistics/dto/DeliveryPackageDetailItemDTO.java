package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryPackageDetailItemDTO {
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    private String remark;
}