package com.supplier.portal.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.query.PortalTodoQuery;
import com.supplier.portal.service.PortalTodoService;
import com.supplier.portal.vo.PortalTodoVO;
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
@RestController
@RequestMapping("/v1/todos")
@RequiredArgsConstructor
public class PortalTodoController {
    private final PortalTodoService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<PortalTodoVO>> page(@Valid PortalTodoQuery query) { return Result.success(service.page(query)); }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> unreadCount() { return Result.success(service.unreadCount()); }

    @PostMapping("/{id}/finish")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> finish(@PathVariable @NotNull Long id) { service.finish(id); return Result.success(); }

    @PostMapping("/{id}/ignore")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> ignore(@PathVariable @NotNull Long id) { service.ignore(id); return Result.success(); }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody PortalTodoCreateDTO dto) { return Result.success(service.create(dto)); }
}
