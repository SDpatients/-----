package com.supplier.order.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTrackVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private Integer trackStatus;
    private LocalDateTime trackTime;
    private String trackRemark;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operator;
    private String operatorName;
}