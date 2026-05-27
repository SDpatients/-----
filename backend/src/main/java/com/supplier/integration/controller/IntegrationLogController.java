package com.supplier.integration.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.integration.query.IntegrationLogQuery;
import com.supplier.integration.service.IntegrationLogService;
import com.supplier.integration.vo.IntegrationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "集成调用日志", description = "集成调用日志查询接口")
@RestController
@RequestMapping("/v1/integration-logs")
@RequiredArgsConstructor
public class IntegrationLogController {

    private final IntegrationLogService integrationLogService;

    @Operation(summary = "分页查询集成日志")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<IntegrationLogVO>> page(@Valid IntegrationLogQuery query) {
        return Result.success(integrationLogService.page(query));
    }

    @Operation(summary = "查询集成日志详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<IntegrationLogVO> detail(@PathVariable @NotNull(message = "日志ID不能为空") Long id) {
        return Result.success(integrationLogService.getDetail(id));
    }
}