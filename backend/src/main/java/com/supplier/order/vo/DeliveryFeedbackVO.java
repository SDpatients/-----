package com.supplier.order.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeliveryFeedbackVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long supplierId;
    private Integer feedbackStatus;
    private String remark;
    private Long buyerConfirmBy;
    private LocalDateTime buyerConfirmTime;
    private List<DeliveryFeedbackLineVO> lines;
}