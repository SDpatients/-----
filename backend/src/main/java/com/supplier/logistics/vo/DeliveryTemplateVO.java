package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class DeliveryTemplateVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String templateName;
    private String templateCode;
    private String description;
    private String headerConfig;
    private String footerConfig;
    private String columnConfig;
    private Boolean isDefault;
    private String remark;
}