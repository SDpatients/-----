package com.supplier.integration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThirdPartyPoApiConfigCreateDTO {

    @NotBlank(message = "配置名称不能为空")
    private String configName;

    @NotBlank(message = "接口类型不能为空")
    private String apiType;

    @NotBlank(message = "接口地址不能为空")
    private String baseUrl;

    @NotBlank(message = "HTTP方法不能为空")
    private String httpMethod;

    private String authType = "NONE";
    private String authCredentials;
    private String requestHeaders;
    private String requestBodyTemplate;

    @NotNull(message = "超时时间不能为空")
    private Integer timeoutSeconds;

    private Integer retryCount;
    private String remark;
}