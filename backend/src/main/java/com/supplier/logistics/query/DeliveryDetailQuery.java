package com.supplier.logistics.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryDetailQuery {
    @NotNull(message = "送货通知ID不能为空")
    private Long noticeId;
}
