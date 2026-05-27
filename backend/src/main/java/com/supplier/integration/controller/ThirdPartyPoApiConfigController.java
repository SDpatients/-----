package com.supplier.integration.controller;

import com.supplier.common.result.Result;
import com.supplier.integration.dto.ThirdPartyPoApiConfigCreateDTO;
import com.supplier.integration.dto.ThirdPartyPoApiConfigUpdateDTO;
import com.supplier.integration.service.ThirdPartyPoApiConfigService;
import com.supplier.integration.vo.ThirdPartyPoApiConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "第三方采购订单接口配置", description = "第三方采购订单接口配置管理")
@RestController
@RequestMapping("/v1/third-party-po-config")
@RequiredArgsConstructor
public class ThirdPartyPoApiConfigController {

    private final ThirdPartyPoApiConfigService service;

    @Operation(summary = "查询配置列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<ThirdPartyPoApiConfigVO>> list(@RequestParam(required = false) String apiType) {
        return Result.success(service.list(apiType));
    }

    @Operation(summary = "查询配置详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ThirdPartyPoApiConfigVO> detail(@PathVariable @NotNull(message = "配置ID不能为空") Long id) {
        return Result.success(service.getDetail(id));
    }

    @Operation(summary = "新增接口配置")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody ThirdPartyPoApiConfigCreateDTO dto) {
        return Result.success(service.create(dto));
    }

    @Operation(summary = "修改接口配置")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "配置ID不能为空") Long id,
                               @Valid @RequestBody ThirdPartyPoApiConfigUpdateDTO dto) {
        service.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除接口配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "配置ID不能为空") Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用接口配置")
    @PostMapping("/{id}/enable")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> enable(@PathVariable @NotNull(message = "配置ID不能为空") Long id) {
        service.enable(id);
        return Result.success();
    }

    @Operation(summary = "禁用接口配置")
    @PostMapping("/{id}/disable")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> disable(@PathVariable @NotNull(message = "配置ID不能为空") Long id) {
        service.disable(id);
        return Result.success();
    }
}