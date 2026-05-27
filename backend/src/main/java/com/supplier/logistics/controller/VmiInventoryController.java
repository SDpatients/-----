package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.VmiInventorySyncDTO;
import com.supplier.logistics.query.VmiInventoryQuery;
import com.supplier.logistics.service.VmiInventoryService;
import com.supplier.logistics.vo.VmiInventoryVO;
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
@Tag(name = "VMI库存管理", description = "VMI库存查询和同步接口")
@RestController
@RequestMapping("/v1/vmi-inventories")
@RequiredArgsConstructor
public class VmiInventoryController {

    private final VmiInventoryService vmiInventoryService;

    @Operation(summary = "分页查询VMI库存")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<VmiInventoryVO>> page(@Valid VmiInventoryQuery query) {
        return Result.success(vmiInventoryService.page(query));
    }

    @Operation(summary = "查询VMI库存详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<VmiInventoryVO> detail(@PathVariable @NotNull(message = "库存ID不能为空") Long id) {
        return Result.success(vmiInventoryService.getDetail(id));
    }

    @Operation(summary = "同步VMI库存")
    @PostMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> sync(@Valid @RequestBody VmiInventorySyncDTO dto) {
        vmiInventoryService.sync(dto);
        return Result.success();
    }
}