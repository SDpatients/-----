package com.supplier.integration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IntegrationEndpointCreateDTO {

    @NotBlank(message = "端点编码不能为空")
    private String endpointCode;

    @NotBlank(message = "端点名称不能为空")
    private String endpointName;

    @NotBlank(message = "系统类型不能为空")
    private String systemType;

    @NotBlank(message = "集成模式不能为空")
    private String integrationMode;

    private String baseUrl;
    private String authType;

    @NotNull(message = "超时时间不能为空")
    private Integer timeoutMs;

    @NotNull(message = "重试次数不能为空")
    private Integer retryLimit;

    private String remark;
}