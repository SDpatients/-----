package com.supplier.order.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private LocalDateTime confirmTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long buyerId;
    private String buyerName;
    private String deliveryAddress;
    private String remark;

    /** 订单明细总数量（所有明细行quantity之和） */
    private BigDecimal totalQty;
    /** 已发货总数量（所有明细行deliveredQty之和） */
    private BigDecimal shippedQty;
    /** 已收货总数量（所有明细行receivedQty之和） */
    private BigDecimal receivedQty;
    /** 在途总数量（已发-已收） */
    private BigDecimal inTransitQty;
    /** 订单明细行列表 */
    private java.util.List<PurchaseOrderDetailVO> details;
}
