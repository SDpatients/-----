package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryPackageDetailVO {
    private Long id;
    private Long packageId;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    private String remark;
}