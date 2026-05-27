package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_feedback")
public class DeliveryFeedback extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private Long supplierId;
    private Integer feedbackStatus;
    private String remark;
    private Long buyerConfirmBy;
    private LocalDateTime buyerConfirmTime;
}