package com.supplier.message.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.message.entity.MessageTemplate;
import com.supplier.message.service.MessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "消息模板")
@RestController
@RequestMapping("/v1/message-templates")
@RequiredArgsConstructor
public class MessageTemplateController {
    private final MessageTemplateService service;

    @Operation(summary = "分页查询消息模板")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<MessageTemplate>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer channel) {
        return Result.success(service.page(pageNum, pageSize, keyword, channel));
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<MessageTemplate> detail(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @Operation(summary = "新增消息模板")
    @PostMapping
    @PreAuthorize("hasAuthority('config:manage')")
    public Result<Long> create(@Valid @RequestBody MessageTemplate template) {
        return Result.success(service.create(template));
    }

    @Operation(summary = "更新消息模板")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('config:manage')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MessageTemplate template) {
        template.setId(id);
        service.update(template);
        return Result.success();
    }

    @Operation(summary = "删除消息模板")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('config:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "启用/停用模板")
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('config:manage')")
    public Result<Void> toggle(@PathVariable Long id) {
        service.toggleStatus(id);
        return Result.success();
    }

    @Operation(summary = "按业务类型查询启用的模板列表")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<MessageTemplate>> list(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Integer channel) {
        return Result.success(service.listByBusinessType(businessType, channel));
    }
}