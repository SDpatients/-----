package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_detail_version")
public class OrderDetailVersion extends BaseEntity {

    private Long orderId;
    private Long orderDetailId;
    private Integer versionNo;
    private String changeType;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private LocalDate deliveryDate;
    private String snapshotJson;
    private String changeReason;
    private Long changeBy;
}