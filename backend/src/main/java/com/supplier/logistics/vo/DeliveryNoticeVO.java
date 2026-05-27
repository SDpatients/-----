package com.supplier.logistics.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeliveryNoticeVO {
    private Long id;
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
    private String deliveryAddress;
    private String receiver;
    private LocalDateTime sendTime;
    private LocalDateTime arriveTime;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String remark;
    private List<DeliveryDetailVO> details;
}
