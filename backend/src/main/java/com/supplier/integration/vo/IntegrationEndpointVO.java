package com.supplier.integration.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class IntegrationEndpointVO {

    @JsonSerialize(using = ToStringSerializer.class)
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