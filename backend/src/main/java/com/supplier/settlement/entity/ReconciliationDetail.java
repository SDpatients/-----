package com.supplier.settlement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reconciliation_detail")
public class ReconciliationDetail extends BaseEntity {
    private Long reconId;
    private Long orderId;
    private String orderNo;
    @TableField("receipt_id")
    private Long deliveryId;
    @TableField("receipt_no")
    private String deliveryNo;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal orderAmount;
    private BigDecimal deductionAmount;
    private BigDecimal confirmedAmount;
    private BigDecimal diffAmount;
    private String diffReason;
    private Integer confirmStatus;
    private LocalDateTime confirmTime;
    private String confirmRemark;
    private String remark;
}
