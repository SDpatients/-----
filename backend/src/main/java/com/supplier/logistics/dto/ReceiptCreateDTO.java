package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReceiptCreateDTO {
    @NotNull(message = "送货明细ID不能为空")
    private Long deliveryId;
    private Long noticeId;
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private BigDecimal planQty = BigDecimal.ZERO;
    private BigDecimal receiptQty = BigDecimal.ZERO;
    private BigDecimal rejectQty = BigDecimal.ZERO;

    @NotNull(message = "收货时间不能为空")
    private LocalDateTime receiptTime;
    private Long warehouseId;
    private String warehouseName;
    private String location;
    private String remark;
}
