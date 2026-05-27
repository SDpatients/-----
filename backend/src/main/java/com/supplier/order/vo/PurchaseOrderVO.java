package com.supplier.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderVO {

    private Long id;
    private String orderNo;
    private Long supplierId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private LocalDateTime confirmTime;
    private Long buyerId;
    private String buyerName;
    private String deliveryAddress;
    private String remark;
}
