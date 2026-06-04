package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WriteOffCreateDTO {
    private Long noticeId;
    private String asnNo;
    private Long orderId;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    @NotBlank(message = "冲销类型不能为空")
    private String writeOffType;
    @NotNull(message = "冲销金额不能为空")
    private BigDecimal amount;
    @NotBlank(message = "冲销原因不能为空")
    private String reason;
    private String remark;
}
