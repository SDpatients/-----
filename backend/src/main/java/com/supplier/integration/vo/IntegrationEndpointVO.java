package com.supplier.integration.vo;

import lombok.Data;

@Data
public class IntegrationEndpointVO {

    private Long id;
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