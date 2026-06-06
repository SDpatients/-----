package com.supplier.system.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.system.dto.SysUserCreateDTO;
import com.supplier.system.dto.SysUserPasswordDTO;
import com.supplier.system.query.SysUserQuery;
import com.supplier.system.service.SysUserService;
import com.supplier.system.vo.SysUserVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "供应商账号管理", description = "供应商账号增删改查接口")
@RestController
@RequestMapping("/v1/supplier-accounts")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询供应商账号")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SysUserVO>> page(@Valid SysUserQuery query) {
        return Result.success(sysUserService.page(query));
    }

    @Operation(summary = "查询账号详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysUserVO> detail(@PathVariable @NotNull(message = "账号ID不能为空") Long id) {
        return Result.success(sysUserService.getDetail(id));
    }

    @Operation(summary = "新增供应商账号")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SysUserCreateDTO dto) {
        return Result.success(sysUserService.create(dto));
    }

    @Operation(summary = "重置账号密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> resetPassword(@PathVariable @NotNull(message = "账号ID不能为空") Long id,
                                      @Valid @RequestBody SysUserPasswordDTO dto) {
        sysUserService.resetPassword(id, dto);
        return Result.success();
    }

    @Operation(summary = "启用/禁用账号")
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> toggleStatus(@PathVariable @NotNull(message = "账号ID不能为空") Long id,
                                     @NotNull(message = "状态不能为空") @RequestParam Integer status) {
        sysUserService.toggleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除供应商账号")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "账号ID不能为空") Long id) {
        sysUserService.delete(id);
        return Result.success();
    }
}