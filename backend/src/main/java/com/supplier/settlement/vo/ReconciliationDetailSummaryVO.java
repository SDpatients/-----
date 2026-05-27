package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 对账明细行摘要 VO
 */
@Data
public class ReconciliationDetailSummaryVO {

    /** 来源类型: receipt/return/deduction */
    private String sourceType;

    /** 来源单号 */
    private String sourceNo;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 金额 */
    private BigDecimal amount;

    /** 差异金额 */
    private BigDecimal diffAmount;

    /** 差异原因 */
    private String diffReason;

    /** 备注 */
    private String remark;
}