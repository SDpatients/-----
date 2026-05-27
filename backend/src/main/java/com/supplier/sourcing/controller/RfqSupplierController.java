package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.RfqSupplierCreateDTO;
import com.supplier.sourcing.query.RfqSupplierQuery;
import com.supplier.sourcing.service.RfqSupplierService;
import com.supplier.sourcing.vo.RfqSupplierVO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "询价供应商管理", description = "询价单邀请供应商管理")
@RestController
@RequestMapping("/v1/rfq-suppliers")
@RequiredArgsConstructor
public class RfqSupplierController {

    private final RfqSupplierService rfqSupplierService;

    @Operation(summary = "分页查询关联供应商")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<RfqSupplierVO>> page(@Valid RfqSupplierQuery query) {
        return Result.success(rfqSupplierService.page(query));
    }

    @Operation(summary = "查询关联供应商详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<RfqSupplierVO> detail(@PathVariable @NotNull(message = "供应商关联ID不能为空") Long id) {
        return Result.success(rfqSupplierService.getDetail(id));
    }

    @Operation(summary = "邀请供应商")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody RfqSupplierCreateDTO dto) {
        return Result.success(rfqSupplierService.create(dto));
    }

    @Operation(summary = "移除关联供应商")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "供应商关联ID不能为空") Long id) {
        rfqSupplierService.delete(id);
        return Result.success();
    }
}