package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuoteSubmitDTO {

    @NotNull(message = "报价单ID不能为空")
    private Long quoteId;
}