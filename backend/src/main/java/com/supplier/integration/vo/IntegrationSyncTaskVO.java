package com.supplier.integration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationSyncTaskVO {

    private Long id;
    private String taskNo;
    private String systemType;
    private String taskType;
    private String externalNo;
    private String eventType;
    private String payload;
    private Integer taskStatus;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private String errorMessage;
    private LocalDateTime createTime;
}