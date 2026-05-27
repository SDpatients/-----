package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order_erp_mapping")
public class PurchaseOrderErpMapping extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private String erpOrderNo;
    private String erpSystemType;
    private LocalDateTime syncTime;
}