package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_plan")
public class DeliveryPlan extends BaseEntity {
    private Long orderId;
    private Long orderDetailId;
    private Long supplierId;
    private LocalDate planDate;
    private BigDecimal planQty;
    private LocalDate promiseDate;
    private Integer planStatus;
    private String remark;
}