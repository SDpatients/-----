package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 对账汇总结果 VO
 */
@Data
public class ReconciliationSummaryVO {

    private Long reconId;
    private String reconNo;
    private Long supplierId;
    private String supplierName;
    private String reconPeriod;
    private LocalDate startDate;
    private LocalDate endDate;

    /** 收货总金额 */
    private BigDecimal totalReceiptAmount;

    /** 退货总金额 */
    private BigDecimal totalReturnAmount;

    /** 扣款总金额 */
    private BigDecimal totalDeductionAmount;

    /** 应付金额 = 收货 - 退货 - 扣款 */
    private BigDecimal payableAmount;

    /** 收货明细数 */
    private Integer receiptCount;

    /** 退货明细数 */
    private Integer returnCount;

    /** 扣款明细数 */
    private Integer deductionCount;

    /** 对账明细行摘要列表 */
    private List<ReconciliationDetailSummaryVO> details;
}