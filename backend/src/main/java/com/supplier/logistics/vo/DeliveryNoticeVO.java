package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeliveryNoticeVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String noticeNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
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
