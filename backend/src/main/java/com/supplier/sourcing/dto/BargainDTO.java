package com.supplier.sourcing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BargainDTO {

    @NotNull(message = "目标价格不能为空")
    @JsonProperty("targetAmount")
    private BigDecimal targetPrice;

    @JsonProperty("message")
    private String buyerRemark;
}