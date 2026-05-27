package com.supplier.integration.dto;

import lombok.Data;

@Data
public class IntegrationEndpointUpdateDTO {

    private String endpointName;
    private String systemType;
    private String integrationMode;
    private String baseUrl;
    private String authType;
    private Integer timeoutMs;
    private Integer retryLimit;
    private String remark;
}