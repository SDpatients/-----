package com.supplier.settlement.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReconciliationDetailQuery {
    @NotNull(message = "对账单ID不能为空")
    private Long reconId;
}
