package com.supplier.system.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.system.query.SysAuditLogQuery;
import com.supplier.system.service.SysAuditLogService;
import com.supplier.system.vo.SysAuditLogVO;
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
@RestController
@RequestMapping("/v1/audit-logs")
@RequiredArgsConstructor
public class SysAuditLogController {
    private final SysAuditLogService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SysAuditLogVO>> page(@Valid SysAuditLogQuery query) {
        return Result.success(service.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysAuditLogVO> detail(@PathVariable @NotNull(message = "审计日志ID不能为空") Long id) {
        return Result.success(service.getDetail(id));
    }
}
