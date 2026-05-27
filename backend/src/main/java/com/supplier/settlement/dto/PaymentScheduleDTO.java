package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentScheduleDTO {

    @NotNull(message = "计划付款日期不能为空")
    private LocalDate scheduleDate;

    private String paymentTerms;
}