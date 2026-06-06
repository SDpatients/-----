package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.PaymentStatusUpdateDTO;
import com.supplier.logistics.dto.ReconciliationConfirmDTO;
import com.supplier.logistics.dto.ReconciliationStatusUpdateDTO;
import com.supplier.logistics.query.FinancialReconciliationQuery;
import com.supplier.logistics.service.FinancialReconciliationService;
import com.supplier.logistics.vo.FinancialReconciliationChartDataVO;
import com.supplier.logistics.vo.FinancialReconciliationOverviewVO;
import com.supplier.logistics.vo.FinancialReconciliationRecordVO;
import com.supplier.logistics.vo.ReconciliationDetailVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/financial-reconciliation")
@RequiredArgsConstructor
public class FinancialReconciliationController {

    private final FinancialReconciliationService financialReconciliationService;

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public Result<FinancialReconciliationOverviewVO> overview(@Valid FinancialReconciliationQuery query) {
        return Result.success(financialReconciliationService.getOverview(query));
    }

    @GetMapping("/chart-data")
    @PreAuthorize("isAuthenticated()")
    public Result<FinancialReconciliationChartDataVO> chartData(@Valid FinancialReconciliationQuery query) {
        return Result.success(financialReconciliationService.getChartData(query));
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<FinancialReconciliationRecordVO>> list(@Valid FinancialReconciliationQuery query) {
        return Result.success(financialReconciliationService.page(query));
    }

    /** 更新对账状态（含差异金额） */
    @PutMapping("/{id}/reconciliation-status")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateReconciliationStatus(@PathVariable @NotNull(message = "ID不能为空") Long id,
                                                    @Valid @RequestBody ReconciliationStatusUpdateDTO dto) {
        financialReconciliationService.updateReconciliationStatus(id, dto);
        return Result.success();
    }

    /** 更新付款状态 */
    @PutMapping("/{id}/payment-status")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updatePaymentStatus(@PathVariable @NotNull(message = "ID不能为空") Long id,
                                            @Valid @RequestBody PaymentStatusUpdateDTO dto) {
        financialReconciliationService.updatePaymentStatus(id, dto.getPaymentStatus());
        return Result.success();
    }

    /** 获取对账明细详情（含物料行） */
    @GetMapping("/{id}/detail")
    @PreAuthorize("isAuthenticated()")
    public Result<ReconciliationDetailVO> getDetail(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        return Result.success(financialReconciliationService.getDetail(id));
    }

    /** 提交对账确认（含各行对账数量，自动计算差异金额） */
    @PutMapping("/{id}/confirm-reconciliation")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirmReconciliation(@PathVariable @NotNull(message = "ID不能为空") Long id,
                                               @Valid @RequestBody ReconciliationConfirmDTO dto) {
        financialReconciliationService.confirmReconciliation(id, dto);
        return Result.success();
    }
}
