package com.supplier.settlement.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentQuery {

    @Min(value = 1, message = "页码不能小于1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Long pageSize = 10L;

    private Long supplierId;
    private Long reconId;
    private Long invoiceId;
    private Integer paymentStatus;
    private Integer approveStatus;
    private LocalDate scheduleDateStart;
    private LocalDate scheduleDateEnd;
    private String keyword;
}