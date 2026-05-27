package com.supplier.integration.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.integration.dto.SyncTaskRetryDTO;
import com.supplier.integration.query.IntegrationSyncTaskQuery;
import com.supplier.integration.service.IntegrationSyncTaskService;
import com.supplier.integration.vo.IntegrationSyncTaskVO;
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
@Tag(name = "集成同步任务", description = "集成同步任务查询和重试接口")
@RestController
@RequestMapping("/v1/integration-sync-tasks")
@RequiredArgsConstructor
public class IntegrationSyncTaskController {

    private final IntegrationSyncTaskService integrationSyncTaskService;

    @Operation(summary = "分页查询同步任务")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<IntegrationSyncTaskVO>> page(@Valid IntegrationSyncTaskQuery query) {
        return Result.success(integrationSyncTaskService.page(query));
    }

    @Operation(summary = "查询同步任务详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<IntegrationSyncTaskVO> detail(@PathVariable @NotNull(message = "任务ID不能为空") Long id) {
        return Result.success(integrationSyncTaskService.getDetail(id));
    }

    @Operation(summary = "重试同步任务")
    @PostMapping("/{id}/retry")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> retry(@PathVariable @NotNull(message = "任务ID不能为空") Long id,
                              @RequestBody SyncTaskRetryDTO dto) {
        integrationSyncTaskService.retry(id, dto);
        return Result.success();
    }
}