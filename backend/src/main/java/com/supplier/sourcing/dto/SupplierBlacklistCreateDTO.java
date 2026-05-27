package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierBlacklistCreateDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private String creditCode;

    @NotBlank(message = "拉黑原因不能为空")
    private String reason;

    @NotNull(message = "生效开始时间不能为空")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}