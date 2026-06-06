package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 对账明细详情 —— 包含物流单基本信息 + 物料明细行
 */
@Data
public class ReconciliationDetailVO {
    /** delivery_notice.id */
    private Long id;
    /** ASN单号 */
    private String asnNo;
    /** 采购订单号 */
    private String orderNo;
    /** 供应商名称 */
    private String supplierName;
    /** 发货总金额 */
    private BigDecimal totalAmount;
    /** 币种 */
    private String currency;
    /** 对账状态 */
    private Integer reconciliationStatus;
    /** 付款状态 */
    private Integer paymentStatus;
    /** 对账期间 */
    private String period;
    /** 物料明细行 */
    private List<ReconciliationLineVO> lines;
    /** 汇总：订单总金额 */
    private BigDecimal totalOrderAmount;
    /** 汇总：对账总金额 */
    private BigDecimal totalReconcileAmount;
    /** 汇总：差异总金额 */
    private BigDecimal totalDiffAmount;
}
