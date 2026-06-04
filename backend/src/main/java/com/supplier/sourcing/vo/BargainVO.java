package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BargainVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long quoteId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqId;
    private String fromUserType;
    private String fromUserName;
    private String action;
    private String message;
    private BigDecimal targetPrice;
    private BigDecimal supplierPrice;
    private LocalDateTime createTime;
}