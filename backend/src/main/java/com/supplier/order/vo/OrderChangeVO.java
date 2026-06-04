package com.supplier.order.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderChangeVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderDetailId;
    private Integer changeType;
    private String changeContent;
    private String beforeValue;
    private String afterValue;
    private String changeReason;
    private Integer approveStatus;
    private String approveRemark;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
}
