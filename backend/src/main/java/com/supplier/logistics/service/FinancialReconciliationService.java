package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.ReconciliationConfirmDTO;
import com.supplier.logistics.dto.ReconciliationStatusUpdateDTO;
import com.supplier.logistics.query.FinancialReconciliationQuery;
import com.supplier.logistics.vo.FinancialReconciliationChartDataVO;
import com.supplier.logistics.vo.FinancialReconciliationOverviewVO;
import com.supplier.logistics.vo.FinancialReconciliationRecordVO;
import com.supplier.logistics.vo.ReconciliationDetailVO;

public interface FinancialReconciliationService {

    FinancialReconciliationOverviewVO getOverview(FinancialReconciliationQuery query);

    FinancialReconciliationChartDataVO getChartData(FinancialReconciliationQuery query);

    PageResult<FinancialReconciliationRecordVO> page(FinancialReconciliationQuery query);

    void updateReconciliationStatus(Long id, ReconciliationStatusUpdateDTO dto);

    void updatePaymentStatus(Long id, Integer paymentStatus);

    /** 获取对账明细详情（含物料行） */
    ReconciliationDetailVO getDetail(Long id);

    /** 提交对账确认（含各行对账数量，自动计算差异金额） */
    void confirmReconciliation(Long id, ReconciliationConfirmDTO dto);
}
