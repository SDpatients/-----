package com.supplier.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseOrderDetailVO {
    private Long id;
    private Long orderId;
    private Integer lineNo;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String materialModel;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amount;
    private BigDecimal deliveredQty;
    private BigDecimal receivedQty;
    private BigDecimal qualifiedQty;
    private LocalDate deliveryDate;
    private String remark;
}
