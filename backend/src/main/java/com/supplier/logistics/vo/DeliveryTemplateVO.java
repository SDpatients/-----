package com.supplier.logistics.vo;

import lombok.Data;

@Data
public class DeliveryTemplateVO {
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