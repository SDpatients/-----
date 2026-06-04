package com.supplier.order.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryFeedbackLineVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feedbackId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderDetailId;
    private String materialCode;
    private String materialName;
    private LocalDate promisedDeliveryDate;
    private BigDecimal plannedQuantity;
    private String batchNo;
    private String remark;
}