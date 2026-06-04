package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RfqSupplierVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String supplierName;
    private Integer inviteStatus;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long quoteId;
    private LocalDateTime inviteTime;
    private LocalDateTime responseTime;
    private LocalDateTime createTime;
}