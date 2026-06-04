package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long noticeId;
    @JsonSerialize(using = ToStringSerializer.class)
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
    private Integer boxCount;
    private String caseNo;
    private Integer qtyPerCase;
    private String barcode;
    private String remark;
}
