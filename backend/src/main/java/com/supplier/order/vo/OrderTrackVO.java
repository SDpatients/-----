package com.supplier.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTrackVO {

    private Long id;
    private Long orderId;
    private Integer trackStatus;
    private LocalDateTime trackTime;
    private String trackRemark;
    private Long operator;
    private String operatorName;
}