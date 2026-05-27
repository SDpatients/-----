package com.supplier.settlement.service;

import com.supplier.settlement.dto.ReconciliationSummaryDTO;
import com.supplier.settlement.vo.ReconciliationSummaryVO;

/**
 * 对账汇总服务（联动6: 对账周期 → 自动汇总收货/退货/扣款）
 */
public interface ReconciliationSummaryService {

    /**
     * 按对账周期自动汇总指定供应商的所有收货、退货、扣款记录，
     * 生成对账单和明细行
     */
    ReconciliationSummaryVO summarize(ReconciliationSummaryDTO dto);
}