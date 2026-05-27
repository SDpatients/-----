package com.supplier.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryFeedbackLineVO {

    private Long id;
    private Long feedbackId;
    private Long orderDetailId;
    private String materialCode;
    private String materialName;
    private LocalDate promisedDeliveryDate;
    private BigDecimal plannedQuantity;
    private String batchNo;
    private String remark;
}