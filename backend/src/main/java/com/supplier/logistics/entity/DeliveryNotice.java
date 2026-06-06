package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_notice")
public class DeliveryNotice extends BaseEntity {
    private String noticeNo;
    private Long orderId;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private LocalDate planDeliveryDate;
    private LocalDate actualDeliveryDate;
    private Integer deliveryStatus;
    private String deliveryMethod;
    private String deliveryCompany;
    private String deliveryNo;
    private String driverName;
    private String driverPhone;
    private String vehicleNo;
    private String deliveryAddress;
    private String receiver;
    private String receiverPhone;
    private LocalDateTime sendTime;
    private LocalDateTime arriveTime;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private LocalDateTime submitTime;
    private LocalDateTime closeTime;
    private String remark;
    /** 发货总金额 */
    private BigDecimal totalAmount;
    /** 币种 */
    private String currency;
    /** 税额 */
    private BigDecimal taxAmount;
    /** 净额 */
    private BigDecimal netAmount;
    /** 付款状态: 0-未付款 1-部分付款 2-已付款 */
    private Integer paymentStatus;
    /** 对账状态: 0-待对账 1-对账中 2-已对账 3-有差异 */
    private Integer reconciliationStatus;
    /** 收货仓库 */
    private String warehouse;
}
