package com.supplier.integration.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class IntegrationSyncTaskQuery {

    @Min(value = 1, message = "页码不能小于1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Long pageSize = 10L;

    private String systemType;
    private String taskType;
    private Integer taskStatus;
}