package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BargainDTO {

    @NotNull(message = "目标价格不能为空")
    private BigDecimal targetPrice;

    private String buyerRemark;
}