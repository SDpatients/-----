package com.supplier.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderChangeVO {
    private Long id;
    private Long orderId;
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
