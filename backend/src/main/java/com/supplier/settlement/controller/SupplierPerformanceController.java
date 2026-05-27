package com.supplier.settlement.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.settlement.dto.SupplierPerformanceCreateDTO;
import com.supplier.settlement.dto.SupplierPerformanceUpdateDTO;
import com.supplier.settlement.query.SupplierPerformanceQuery;
import com.supplier.settlement.service.SupplierPerformanceService;
import com.supplier.settlement.vo.SupplierPerformanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "供应商绩效")
@RestController
@RequestMapping("/v1/supplier-performances")
@RequiredArgsConstructor
public class SupplierPerformanceController {

    private final SupplierPerformanceService supplierPerformanceService;

    @Operation(summary = "分页查询供应商绩效")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SupplierPerformanceVO>> page(@Valid SupplierPerformanceQuery query) {
        return Result.success(supplierPerformanceService.page(query));
    }

    @Operation(summary = "查询供应商绩效详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SupplierPerformanceVO> detail(@PathVariable @NotNull(message = "绩效ID不能为空") Long id) {
        return Result.success(supplierPerformanceService.getDetail(id));
    }

    @Operation(summary = "新增供应商绩效")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SupplierPerformanceCreateDTO dto) {
        return Result.success(supplierPerformanceService.create(dto));
    }

    @Operation(summary = "更新供应商绩效")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "绩效ID不能为空") Long id,
                               @Valid @RequestBody SupplierPerformanceUpdateDTO dto) {
        supplierPerformanceService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除供应商绩效")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "绩效ID不能为空") Long id) {
        supplierPerformanceService.delete(id);
        return Result.success();
    }
}