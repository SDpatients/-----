package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.SupplierCategoryCreateDTO;
import com.supplier.sourcing.dto.SupplierCategoryUpdateDTO;
import com.supplier.sourcing.query.SupplierCategoryQuery;
import com.supplier.sourcing.service.SupplierCategoryService;
import com.supplier.sourcing.vo.SupplierCategoryVO;
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

import java.util.List;

@Validated
@Tag(name = "供应商品类管理", description = "供应商品类/分类管理接口")
@RestController
@RequestMapping("/v1/supplier-categories")
@RequiredArgsConstructor
public class SupplierCategoryController {

    private final SupplierCategoryService supplierCategoryService;

    @Operation(summary = "分页查询品类")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SupplierCategoryVO>> page(@Valid SupplierCategoryQuery query) {
        return Result.success(supplierCategoryService.page(query));
    }

    @Operation(summary = "查询全部启用品类列表")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SupplierCategoryVO>> listAll() {
        return Result.success(supplierCategoryService.listAll());
    }

    @Operation(summary = "查询品类详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SupplierCategoryVO> detail(@PathVariable @NotNull(message = "品类ID不能为空") Long id) {
        return Result.success(supplierCategoryService.getDetail(id));
    }

    @Operation(summary = "新增品类")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SupplierCategoryCreateDTO dto) {
        return Result.success(supplierCategoryService.create(dto));
    }

    @Operation(summary = "更新品类")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "品类ID不能为空") Long id,
                               @Valid @RequestBody SupplierCategoryUpdateDTO dto) {
        supplierCategoryService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除品类")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "品类ID不能为空") Long id) {
        supplierCategoryService.delete(id);
        return Result.success();
    }
}