package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_package")
public class DeliveryPackage extends BaseEntity {
    private Long noticeId;
    private String packageNo;
    private String packageType;
    private BigDecimal weight;
    private BigDecimal volume;
    private String remark;
}