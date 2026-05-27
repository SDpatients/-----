package com.supplier.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeliveryFeedbackCreateDTO {

    @NotEmpty(message = "交期反馈明细不能为空")
    @Valid
    private List<DeliveryFeedbackLineDTO> lines;

    private String remark;
}