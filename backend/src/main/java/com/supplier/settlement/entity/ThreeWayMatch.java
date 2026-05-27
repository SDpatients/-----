package com.supplier.settlement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 三单匹配结果（联动7: 三单匹配 → 自动匹配订单/收货/发票）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("three_way_match")
public class ThreeWayMatch extends BaseEntity {

    /** 对账单ID */
    private Long reconId;

    /** 采购订单ID */
    private Long orderId;

    /** 采购订单号 */
    private String orderNo;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 收货记录ID */
    private Long receiptId;

    /** 收货金额 */
    private BigDecimal receiptAmount;

    /** 发票ID */
    private Long invoiceId;

    /** 发票号码 */
    private String invoiceNo;

    /** 发票金额 */
    private BigDecimal invoiceAmount;

    /** 供应商ID */
    private Long supplierId;

    /**
     * 匹配结果:
     * 0=完全匹配 (订单>=收货>=发票)
     * 1=部分匹配
     * 2=不匹配
     */
    private Integer matchResult;

    /** 差异金额 */
    private BigDecimal diffAmount;

    /** 差异原因 */
    private String diffReason;

    /** 匹配时间 */
    private java.time.LocalDateTime matchTime;

    /** 备注 */
    private String remark;
}