package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vmi_inventory")
public class VmiInventory extends BaseEntity {

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