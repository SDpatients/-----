package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RfqItemCreateDTO {

    @NotNull(message = "询价单ID不能为空")
    private Long rfqId;

    private Integer lineNo;

    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    @NotBlank(message = "物料名称不能为空")
    private String materialName;

    private String materialSpec;
    private String unit;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    private LocalDate targetDeliveryDate;
    private String remark;
}