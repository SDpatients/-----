package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ForecastDemandCreateDTO {

    private Long supplierId;

    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    @NotNull(message = "需求日期不能为空")
    private LocalDate demandDate;

    private BigDecimal demandQty;
    private Integer demandType;
}