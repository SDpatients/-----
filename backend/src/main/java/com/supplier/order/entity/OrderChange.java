package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_change")
public class OrderChange extends BaseEntity {

    private String changeNo;
    private Long orderId;
    private Long orderDetailId;
    private Integer changeType;
    private String changeContent;
    private String beforeValue;
    private String afterValue;
    private String changeReason;
    private Long applyBy;
    private LocalDateTime applyTime;
    private Long approveBy;
    private LocalDateTime approveTime;
    private Integer approveStatus;
    private String approveRemark;
}
