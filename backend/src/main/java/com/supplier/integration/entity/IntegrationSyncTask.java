package com.supplier.integration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integration_sync_task")
public class IntegrationSyncTask extends BaseEntity {

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
}