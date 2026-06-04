package com.supplier.integration.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ThirdPartyPoApiConfigVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
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
    private Integer enabled;
    private String remark;
}