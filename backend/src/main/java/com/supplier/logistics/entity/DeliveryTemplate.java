package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_template")
public class DeliveryTemplate extends BaseEntity {
    private String templateName;
    private String templateCode;
    private String description;
    private String headerConfig;
    private String footerConfig;
    private String columnConfig;
    private Boolean isDefault;
    private String remark;
}