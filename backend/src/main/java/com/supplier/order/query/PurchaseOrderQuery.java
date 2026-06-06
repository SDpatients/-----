package com.supplier.order.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderQuery {

    @Min(value = 1, message = "页码不能小于1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Long pageSize = 10L;

    private String keyword;
    private Integer orderStatus;
    private List<Integer> excludeStatuses;
    private Long supplierId;
    private LocalDate startDate;
    private LocalDate endDate;
}
