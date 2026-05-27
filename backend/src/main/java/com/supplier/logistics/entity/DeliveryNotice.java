package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private String remark;
}
