package com.supplier.integration.dto;

import lombok.Data;

@Data
public class ThirdPartyPoApiConfigUpdateDTO {

    private String configName;
    private String apiType;
    private String baseUrl;
    private String httpMethod;
    private String authType;
    private String authCredentials;
    private String requestHeaders;
    private String requestBodyTemplate;
    private Integer timeoutSeconds;
    private Integer retryCount;
    private String remark;
}