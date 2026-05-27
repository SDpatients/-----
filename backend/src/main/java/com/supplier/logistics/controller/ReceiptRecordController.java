package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.ReceiptAdjustDTO;
import com.supplier.logistics.dto.ReceiptConfirmDTO;
import com.supplier.logistics.dto.ReceiptCreateDTO;
import com.supplier.logistics.query.ReceiptRecordQuery;
import com.supplier.logistics.service.ReceiptRecordService;
import com.supplier.logistics.vo.ReceiptRecordVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/receipt-records")
@RequiredArgsConstructor
public class ReceiptRecordController {
    private final ReceiptRecordService receiptRecordService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<ReceiptRecordVO>> page(@Valid ReceiptRecordQuery query) {
        return Result.success(receiptRecordService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ReceiptRecordVO> detail(@PathVariable @NotNull(message = "收货记录ID不能为空") Long id) {
        return Result.success(receiptRecordService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody ReceiptCreateDTO dto) {
        return Result.success(receiptRecordService.create(dto));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirm(@PathVariable @NotNull(message = "收货记录ID不能为空") Long id, @RequestBody ReceiptConfirmDTO dto) {
        receiptRecordService.confirm(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "收货记录ID不能为空") Long id, @RequestBody ReceiptConfirmDTO dto) {
        receiptRecordService.reject(id, dto);
        return Result.success();
    }

    @GetMapping("/scan")
    @PreAuthorize("isAuthenticated()")
    public Result<ReceiptRecordVO> scan(@RequestParam @NotNull(message = "条码不能为空") String barcode) {
        return Result.success(receiptRecordService.scan(barcode));
    }

    @PostMapping("/{id}/adjust")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> adjust(@PathVariable @NotNull(message = "收货记录ID不能为空") Long id, @Valid @RequestBody ReceiptAdjustDTO dto) {
        return Result.success(receiptRecordService.adjust(id, dto));
    }
}
