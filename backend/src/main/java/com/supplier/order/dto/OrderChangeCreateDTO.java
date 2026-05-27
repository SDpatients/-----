package com.supplier.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderChangeCreateDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    private Long orderDetailId;
    @NotNull(message = "变更类型不能为空")
    private Integer changeType;
    @NotBlank(message = "变更内容不能为空")
    private String changeContent;
    private String beforeValue;
    private String afterValue;
    private String changeReason;
}
