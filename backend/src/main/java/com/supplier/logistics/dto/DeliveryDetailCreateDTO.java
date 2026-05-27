package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryDetailCreateDTO {
    @NotNull(message = "送货通知ID不能为空")
    private Long noticeId;
    private Long orderDetailId;
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String materialSpec;
    private String unit;
    private BigDecimal planQty = BigDecimal.ZERO;
    private BigDecimal actualQty = BigDecimal.ZERO;
    private BigDecimal receivedQty = BigDecimal.ZERO;
    private BigDecimal qualifiedQty = BigDecimal.ZERO;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String remark;
}
