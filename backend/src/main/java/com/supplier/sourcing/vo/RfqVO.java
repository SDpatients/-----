package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RfqVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String rfqNo;
    private String rfqTitle;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;
    private String currency;
    private LocalDateTime quoteDeadline;
    private Integer rfqStatus;
    private LocalDateTime publishTime;
    private LocalDateTime closeTime;
    private String remark;
    private LocalDateTime createTime;
}