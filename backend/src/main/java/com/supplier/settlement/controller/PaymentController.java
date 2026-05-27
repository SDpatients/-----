package com.supplier.settlement.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.settlement.dto.PaymentActionDTO;
import com.supplier.settlement.dto.PaymentCreateDTO;
import com.supplier.settlement.dto.PaymentScheduleDTO;
import com.supplier.settlement.query.PaymentQuery;
import com.supplier.settlement.service.PaymentService;
import com.supplier.settlement.vo.PaymentVO;
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
@Tag(name = "付款管理")
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "分页查询付款单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<PaymentVO>> page(@Valid PaymentQuery query) {
        return Result.success(paymentService.page(query));
    }

    @Operation(summary = "查询付款单详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<PaymentVO> detail(@PathVariable @NotNull(message = "付款单ID不能为空") Long id) {
        return Result.success(paymentService.getDetail(id));
    }

    @Operation(summary = "新增付款单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody PaymentCreateDTO dto) {
        return Result.success(paymentService.create(dto));
    }

    @Operation(summary = "提交审批")
    @PostMapping("/{id}/submit-approval")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submitForApproval(@PathVariable @NotNull(message = "付款单ID不能为空") Long id) {
        paymentService.submitForApproval(id);
        return Result.success();
    }

    @Operation(summary = "付款排期")
    @PostMapping("/{id}/schedule")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> schedule(@PathVariable @NotNull(message = "付款单ID不能为空") Long id,
                                 @Valid @RequestBody PaymentScheduleDTO dto) {
        paymentService.schedule(id, dto);
        return Result.success();
    }

    @Operation(summary = "付款")
    @PostMapping("/{id}/pay")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> pay(@PathVariable @NotNull(message = "付款单ID不能为空") Long id,
                            @RequestBody PaymentActionDTO dto) {
        paymentService.pay(id, dto);
        return Result.success();
    }

    @Operation(summary = "拒绝付款")
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "付款单ID不能为空") Long id,
                               @RequestBody PaymentActionDTO dto) {
        paymentService.reject(id, dto);
        return Result.success();
    }

    @Operation(summary = "取消付款单")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable @NotNull(message = "付款单ID不能为空") Long id,
                               @RequestBody PaymentActionDTO dto) {
        paymentService.cancel(id, dto);
        return Result.success();
    }
}