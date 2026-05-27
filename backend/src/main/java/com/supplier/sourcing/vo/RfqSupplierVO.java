package com.supplier.sourcing.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RfqSupplierVO {

    private Long id;
    private Long rfqId;
    private Long supplierId;
    private String supplierName;
    private Integer inviteStatus;
    private Long quoteId;
    private LocalDateTime inviteTime;
    private LocalDateTime responseTime;
    private LocalDateTime createTime;
}