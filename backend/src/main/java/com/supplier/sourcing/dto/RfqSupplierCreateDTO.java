package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RfqSupplierCreateDTO {

    @NotNull(message = "询价单ID不能为空")
    private Long rfqId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String supplierName;
}