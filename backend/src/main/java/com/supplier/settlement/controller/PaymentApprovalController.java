package com.supplier.settlement.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.settlement.dto.PaymentApprovalActionDTO;
import com.supplier.settlement.dto.PaymentApprovalSubmitDTO;
import com.supplier.settlement.query.PaymentApprovalQuery;
import com.supplier.settlement.service.PaymentApprovalService;
import com.supplier.settlement.vo.PaymentApprovalVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "付款审批管理")
@RestController
@RequestMapping("/v1/payment-approvals")
@RequiredArgsConstructor
public class PaymentApprovalController {

    private final PaymentApprovalService approvalService;

    @Operation(summary = "分页查询审批记录")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<PaymentApprovalVO>> page(@Valid PaymentApprovalQuery query) {
        return Result.success(approvalService.page(query));
    }

    @Operation(summary = "查询审批记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentApprovalVO> detail(@PathVariable @NotNull(message = "审批记录ID不能为空") Long id) {
        return Result.success(approvalService.getDetail(id));
    }

    @Operation(summary = "提交付款审批")
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submit(@Valid @RequestBody PaymentApprovalSubmitDTO dto) {
        approvalService.submit(dto);
        return Result.success();
    }

    @Operation(summary = "审批通过")
    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> approve(@PathVariable @NotNull(message = "审批记录ID不能为空") Long id,
                                @Valid @RequestBody PaymentApprovalActionDTO dto) {
        approvalService.approve(id, dto);
        return Result.success();
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "审批记录ID不能为空") Long id,
                               @Valid @RequestBody PaymentApprovalActionDTO dto) {
        approvalService.reject(id, dto);
        return Result.success();
    }
}