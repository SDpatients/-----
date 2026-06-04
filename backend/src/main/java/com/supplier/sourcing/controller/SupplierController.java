package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.SupplierCreateDTO;
import com.supplier.sourcing.dto.SupplierRegisterDTO;
import com.supplier.sourcing.dto.SupplierUpdateDTO;
import com.supplier.sourcing.query.SupplierQuery;
import com.supplier.sourcing.service.SupplierService;
import com.supplier.sourcing.vo.SupplierVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "供应商准入", description = "供应商主数据管理接口")
@RestController
@RequestMapping("/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "分页查询供应商")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SupplierVO>> page(@Valid SupplierQuery query) {
        return Result.success(supplierService.page(query));
    }

    @Operation(summary = "查询供应商详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SupplierVO> detail(@PathVariable @NotNull(message = "供应商ID不能为空") Long id) {
        return Result.success(supplierService.getDetail(id));
    }

    @Operation(summary = "新增供应商（管理员操作）")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SupplierCreateDTO dto) {
        return Result.success(supplierService.create(dto));
    }

    @Operation(summary = "供应商自助注册")
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public Result<Long> register(@Valid @RequestBody SupplierRegisterDTO dto) {
        return Result.success(supplierService.register(dto));
    }

    @Operation(summary = "禁用/启用供应商")
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> toggleStatus(@PathVariable @NotNull(message = "供应商ID不能为空") Long id,
                                     @NotNull(message = "状态不能为空") @RequestParam Integer status) {
        supplierService.toggleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "更新供应商信息")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "供应商ID不能为空") Long id,
                               @Valid @RequestBody SupplierUpdateDTO dto) {
        supplierService.update(id, dto);
        return Result.success();
    }
}