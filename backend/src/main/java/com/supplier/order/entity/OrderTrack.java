package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_track")
public class OrderTrack extends BaseEntity {

    private Long orderId;
    private Integer trackStatus;
    private LocalDateTime trackTime;
    private String trackRemark;
    private Long operator;
    private String operatorName;
}
