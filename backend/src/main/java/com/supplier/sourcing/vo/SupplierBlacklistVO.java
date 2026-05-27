package com.supplier.sourcing.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierBlacklistVO {

    private Long id;
    private Long supplierId;
    private String supplierName;
    private String creditCode;
    private String reason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createTime;
}