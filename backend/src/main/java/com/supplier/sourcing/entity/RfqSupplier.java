package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rfq_supplier")
public class RfqSupplier extends BaseEntity {

    private Long rfqId;
    private Long supplierId;
    private String supplierName;
    private Integer inviteStatus;
    private Long quoteId;
    private LocalDateTime inviteTime;
    private LocalDateTime responseTime;
}