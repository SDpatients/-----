package com.supplier.order.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeliveryFeedbackVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private Integer feedbackStatus;
    private String remark;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long buyerConfirmBy;
    private LocalDateTime buyerConfirmTime;
    private List<DeliveryFeedbackLineVO> lines;
}