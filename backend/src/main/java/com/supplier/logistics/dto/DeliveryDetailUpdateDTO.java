package com.supplier.logistics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryDetailUpdateDTO {
    private Long orderDetailId;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String unit;
    private BigDecimal planQty;
    private BigDecimal actualQty;
    private BigDecimal receivedQty;
    private BigDecimal qualifiedQty;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String remark;
}
