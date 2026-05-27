package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {

    private String orderNo;
    private Long supplierId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private LocalDateTime confirmTime;
    private Long confirmBy;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Long buyerId;
    private String buyerName;
    private Long deptId;
    private String deptName;
    private String contractNo;
    private String paymentTerms;
    private String deliveryAddress;
    private String remark;
}
