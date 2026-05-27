package com.supplier.logistics.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceiptConfirmDTO {
    private BigDecimal receiptQty = BigDecimal.ZERO;
    private BigDecimal rejectQty = BigDecimal.ZERO;
    private Long warehouseId;
    private String warehouseName;
    private String location;
    private String rejectReason;
    private String remark;
}
