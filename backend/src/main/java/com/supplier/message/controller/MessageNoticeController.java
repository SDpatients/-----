package com.supplier.message.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.query.MessageNoticeQuery;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.message.vo.MessageNoticeVO;
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
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
public class MessageNoticeController {
    private final MessageNoticeService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<MessageNoticeVO>> page(@Valid MessageNoticeQuery query) { return Result.success(service.page(query)); }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<MessageNoticeVO> detail(@PathVariable @NotNull Long id) { return Result.success(service.getDetail(id)); }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> unreadCount() { return Result.success(service.unreadCount()); }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody MessageNoticeCreateDTO dto) { return Result.success(service.create(dto)); }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markRead(@PathVariable @NotNull Long id) { service.markRead(id); return Result.success(); }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAllRead() { service.markAllRead(); return Result.success(); }
}
