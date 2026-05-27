package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InspectionStandardItemDTO {

    @NotBlank(message = "检验项目不能为空")
    private String itemName;

    private Integer itemType = 1;
    private String standardValue;
    private BigDecimal upperLimit;
    private BigDecimal lowerLimit;
    private String unit;
    private Integer required = 1;
    private Integer sort;

    @NotNull(message = "标准ID不能为空")
    private Long standardId;
}