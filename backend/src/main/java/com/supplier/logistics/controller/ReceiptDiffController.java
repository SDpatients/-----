package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.ReceiptAdjustDTO;
import com.supplier.logistics.dto.ReceiptDiffApproveDTO;
import com.supplier.logistics.query.ReceiptDiffQuery;
import com.supplier.logistics.service.ReceiptDiffService;
import com.supplier.logistics.vo.ReceiptDiffVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "收货差异管理")
@RestController
@RequestMapping("/v1/receipt-diffs")
@RequiredArgsConstructor
public class ReceiptDiffController {

    private final ReceiptDiffService receiptDiffService;

    @Operation(summary = "分页查询收货差异")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<ReceiptDiffVO>> page(@Valid ReceiptDiffQuery query) {
        return Result.success(receiptDiffService.page(query));
    }

    @Operation(summary = "查询收货差异详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ReceiptDiffVO> detail(@PathVariable @NotNull(message = "差异ID不能为空") Long id) {
        return Result.success(receiptDiffService.getDetail(id));
    }

    @Operation(summary = "创建收货差异调整")
    @PostMapping("/adjust/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> adjust(@PathVariable @NotNull(message = "收货记录ID不能为空") Long recordId,
                               @Valid @RequestBody ReceiptAdjustDTO dto) {
        return Result.success(receiptDiffService.adjust(recordId, dto));
    }

    @Operation(summary = "审批通过收货差异调整")
    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> approve(@PathVariable @NotNull(message = "差异ID不能为空") Long id,
                                @Valid @RequestBody ReceiptDiffApproveDTO dto) {
        receiptDiffService.approve(id, dto);
        return Result.success();
    }

    @Operation(summary = "驳回收货差异调整")
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "差异ID不能为空") Long id,
                               @Valid @RequestBody ReceiptDiffApproveDTO dto) {
        receiptDiffService.reject(id, dto);
        return Result.success();
    }
}