package com.supplier.settlement.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.settlement.dto.InvoiceActionDTO;
import com.supplier.settlement.dto.InvoiceCreateDTO;
import com.supplier.settlement.dto.InvoiceUploadDTO;
import com.supplier.settlement.query.InvoiceQuery;
import com.supplier.settlement.service.InvoiceService;
import com.supplier.settlement.vo.InvoiceVO;
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
@Tag(name = "发票管理")
@RestController
@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "分页查询发票")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<InvoiceVO>> page(@Valid InvoiceQuery query) {
        return Result.success(invoiceService.page(query));
    }

    @Operation(summary = "查询发票详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<InvoiceVO> detail(@PathVariable @NotNull(message = "发票ID不能为空") Long id) {
        return Result.success(invoiceService.getDetail(id));
    }

    @Operation(summary = "新增发票")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody InvoiceCreateDTO dto) {
        return Result.success(invoiceService.create(dto));
    }

    @Operation(summary = "上传发票")
    @PostMapping("/{id}/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> upload(@PathVariable @NotNull(message = "发票ID不能为空") Long id,
                               @Valid @RequestBody InvoiceUploadDTO dto) {
        invoiceService.upload(id, dto);
        return Result.success();
    }

    @Operation(summary = "发票验真")
    @PostMapping("/{id}/verify")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> verify(@PathVariable @NotNull(message = "发票ID不能为空") Long id,
                               @RequestBody InvoiceActionDTO dto) {
        invoiceService.verify(id, dto);
        return Result.success();
    }

    @Operation(summary = "发票认证")
    @PostMapping("/{id}/certify")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> certify(@PathVariable @NotNull(message = "发票ID不能为空") Long id,
                                @RequestBody InvoiceActionDTO dto) {
        invoiceService.certify(id, dto);
        return Result.success();
    }

    @Operation(summary = "发票作废")
    @PostMapping("/{id}/void")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> voidInvoice(@PathVariable @NotNull(message = "发票ID不能为空") Long id,
                                    @RequestBody InvoiceActionDTO dto) {
        invoiceService.voidInvoice(id, dto);
        return Result.success();
    }
}