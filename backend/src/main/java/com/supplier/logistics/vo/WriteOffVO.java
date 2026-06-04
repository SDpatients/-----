package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WriteOffVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String writeOffNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long noticeId;
    private String asnNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String supplierName;
    private String writeOffType;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private LocalDateTime writeOffTime;
    private String remark;
    private LocalDateTime createTime;
}
