package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
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
    /** 收货仓库 */
    private String warehouse;
    /** 发货总数量（由明细 actualQty 聚合，仅在列表场景填充） */
    private BigDecimal quantity;
    private List<DeliveryDetailVO> details;
}
