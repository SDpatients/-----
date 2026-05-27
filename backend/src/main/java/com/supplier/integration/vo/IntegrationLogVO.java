package com.supplier.integration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationLogVO {

    private Long id;
    private String traceId;
    private String endpointCode;
    private String systemType;
    private String interfaceCode;
    private Integer direction;
    private String businessType;
    private Long businessId;
    private String requestSummary;
    private String responseSummary;
    private Integer resultStatus;
    private String errorMessage;
    private Integer costMs;
    private Integer retryCount;
    private LocalDateTime createTime;
}