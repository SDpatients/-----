package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_feedback_line")
public class DeliveryFeedbackLine extends BaseEntity {

    private Long feedbackId;
    private Long orderDetailId;
    private String materialCode;
    private String materialName;
    private LocalDate promisedDeliveryDate;
    private BigDecimal plannedQuantity;
    private String batchNo;
    private String remark;
}