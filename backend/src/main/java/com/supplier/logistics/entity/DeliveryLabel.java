package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_label")
public class DeliveryLabel extends BaseEntity {
    private String labelNo;
    private Long noticeId;
    private Long deliveryDetailId;
    private String materialCode;
    private String batchNo;
    private String packageNo;
    private String palletNo;
    private BigDecimal packageQty;
    private String qrContent;
    private Integer printCount;
    private LocalDateTime lastPrintTime;
}