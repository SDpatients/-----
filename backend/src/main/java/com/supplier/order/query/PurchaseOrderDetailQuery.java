package com.supplier.order.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseOrderDetailQuery {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
