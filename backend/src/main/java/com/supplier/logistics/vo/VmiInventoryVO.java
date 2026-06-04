package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VmiInventoryVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String materialCode;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal onhandQty;
    private BigDecimal availableQty;
    private BigDecimal safetyQty;
    private BigDecimal maxQty;
    private Integer inventoryStatus;
    private LocalDateTime lastSyncTime;
}