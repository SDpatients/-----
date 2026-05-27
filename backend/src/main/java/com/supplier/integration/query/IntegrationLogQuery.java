package com.supplier.integration.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationLogQuery {

    @Min(value = 1, message = "页码不能小于1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Long pageSize = 10L;

    private String systemType;
    private String endpointCode;
    private String interfaceCode;
    private Integer direction;
    private Integer resultStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}