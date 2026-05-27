package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VmiInventoryVO {

    private Long id;
    private Long supplierId;
    private String materialCode;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal onhandQty;
    private BigDecimal availableQty;
    private BigDecimal safetyQty;
    private BigDecimal maxQty;
    private Integer inventoryStatus;
    private LocalDateTime lastSyncTime;
}