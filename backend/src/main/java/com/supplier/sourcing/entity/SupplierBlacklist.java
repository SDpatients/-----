package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_blacklist")
public class SupplierBlacklist extends BaseEntity {

    private Long supplierId;
    private String supplierName;
    private String creditCode;
    private String reason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}