package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PricingDTO {

    @NotNull(message = "报价单ID不能为空")
    private Long quoteId;

    private String remark;
}