package com.supplier.integration.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.integration.dto.IntegrationEndpointCreateDTO;
import com.supplier.integration.dto.IntegrationEndpointUpdateDTO;
import com.supplier.integration.query.IntegrationEndpointQuery;
import com.supplier.integration.service.IntegrationEndpointService;
import com.supplier.integration.vo.IntegrationEndpointVO;
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
@Tag(name = "集成端点配置", description = "集成端点配置管理接口")
@RestController
@RequestMapping("/v1/integration-endpoints")
@RequiredArgsConstructor
public class IntegrationEndpointController {

    private final IntegrationEndpointService integrationEndpointService;

    @Operation(summary = "分页查询集成端点")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<IntegrationEndpointVO>> page(@Valid IntegrationEndpointQuery query) {
        return Result.success(integrationEndpointService.page(query));
    }

    @Operation(summary = "查询集成端点详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<IntegrationEndpointVO> detail(@PathVariable @NotNull(message = "端点ID不能为空") Long id) {
        return Result.success(integrationEndpointService.getDetail(id));
    }

    @Operation(summary = "新增集成端点")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody IntegrationEndpointCreateDTO dto) {
        return Result.success(integrationEndpointService.create(dto));
    }

    @Operation(summary = "修改集成端点")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "端点ID不能为空") Long id,
                               @Valid @RequestBody IntegrationEndpointUpdateDTO dto) {
        integrationEndpointService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除集成端点")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "端点ID不能为空") Long id) {
        integrationEndpointService.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用集成端点")
    @PostMapping("/{id}/enable")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> enable(@PathVariable @NotNull(message = "端点ID不能为空") Long id) {
        integrationEndpointService.enable(id);
        return Result.success();
    }

    @Operation(summary = "禁用集成端点")
    @PostMapping("/{id}/disable")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> disable(@PathVariable @NotNull(message = "端点ID不能为空") Long id) {
        integrationEndpointService.disable(id);
        return Result.success();
    }
}