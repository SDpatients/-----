package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.SupplierBlacklistCreateDTO;
import com.supplier.sourcing.dto.SupplierBlacklistUpdateDTO;
import com.supplier.sourcing.query.SupplierBlacklistQuery;
import com.supplier.sourcing.service.SupplierBlacklistService;
import com.supplier.sourcing.vo.SupplierBlacklistVO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "供应商黑名单", description = "黑名单管理")
@RestController
@RequestMapping("/v1/supplier-blacklists")
@RequiredArgsConstructor
public class SupplierBlacklistController {

    private final SupplierBlacklistService supplierBlacklistService;

    @Operation(summary = "分页查询黑名单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SupplierBlacklistVO>> page(@Valid SupplierBlacklistQuery query) {
        return Result.success(supplierBlacklistService.page(query));
    }

    @Operation(summary = "查询黑名单详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SupplierBlacklistVO> detail(@PathVariable @NotNull(message = "黑名单ID不能为空") Long id) {
        return Result.success(supplierBlacklistService.getDetail(id));
    }

    @Operation(summary = "新增黑名单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SupplierBlacklistCreateDTO dto) {
        return Result.success(supplierBlacklistService.create(dto));
    }

    @Operation(summary = "更新黑名单")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "黑名单ID不能为空") Long id,
                               @Valid @RequestBody SupplierBlacklistUpdateDTO dto) {
        supplierBlacklistService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "移除黑名单")
    @PostMapping("/{id}/remove")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> remove(@PathVariable @NotNull(message = "黑名单ID不能为空") Long id) {
        supplierBlacklistService.remove(id);
        return Result.success();
    }
}