package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_package_detail")
public class DeliveryPackageDetail extends BaseEntity {
    private Long packageId;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    private String remark;
}