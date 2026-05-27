package com.supplier.integration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integration_endpoint")
public class IntegrationEndpoint extends BaseEntity {

    private String endpointCode;
    private String endpointName;
    private String systemType;
    private String integrationMode;
    private String baseUrl;
    private String authType;
    private Integer timeoutMs;
    private Integer retryLimit;
    private Integer status;
    private String remark;
}