package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryTemplateCreateDTO {
    @NotBlank(message = "模板名称不能为空")
    private String templateName;
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;
    private String description;
    private String headerConfig;
    private String footerConfig;
    private String columnConfig;
    private Boolean isDefault;
    private String remark;
}