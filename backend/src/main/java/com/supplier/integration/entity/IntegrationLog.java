package com.supplier.integration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integration_log")
public class IntegrationLog extends BaseEntity {

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
}